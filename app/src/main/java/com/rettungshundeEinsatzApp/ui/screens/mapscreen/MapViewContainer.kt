package com.rettungshundeEinsatzApp.ui.screens.mapscreen

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import org.osmdroid.views.MapView
import com.rettungshundeEinsatzApp.functions.calculatePolygonArea

import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Marker
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rettungshundeEinsatzApp.R
import com.rettungshundeEinsatzApp.database.area.AreaWithCoordinates
import com.rettungshundeEinsatzApp.database.mylocallocation.MyLocationEntity
import com.rettungshundeEinsatzApp.service.myLocation.MyLocationLatLongToMGRS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.component1
import kotlin.collections.component2
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUsersLocationsEntity
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataEntity



/**
 * A reusable MapViewContainer to show areas, tracks and markers.
 */
@SuppressLint("ClickableViewAccessibility")
@Composable
fun MapViewContainer(
    modifier: Modifier = Modifier,

    // my location data
    myLocations: List<MyLocationEntity>,
    myGeoPoints: List<GeoPoint>,

    // all user location data
    allLocations: List<AllUsersLocationsEntity>,
    allUsers: List<AllUserDataEntity>,

    // Areas
    allAreas: List<AreaWithCoordinates>,

    // my track color
    myTrackColor: String,

    // States & Flags
    drawAreaMode: Boolean,
    mapCenteredOnce: Boolean,

    // Function
    markAsCentered: () -> Unit,

    // Area-Editing-States
    areaPoints: SnapshotStateList<GeoPoint>,
    areaPolygon: Polygon,
    areaCornerMarkers: SnapshotStateList<Marker>,

    // Security Level
    securityLevel: Int,

    // User information
    myUserName: String,
    radioCallName: String,

    // Format Strings & Converter
    lastPointText: String,
    radioCallNameText: String,
    accuracyText: String,
    uTMMGRSText: String,
    timeText: String,
    unknownText: String,

    locationToMGRSConverter: MyLocationLatLongToMGRS,

    // Map scale overlay (optional param, if in MapScreen created)
    scaleBarOverlay: ScaleBarOverlay,

    // Time & distance threshold
    timeDiffMillis: Long,
    distanceMeters: Double,


) {


    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Configure osmdroid base path & cache
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val osmConfig = Configuration.getInstance()
            osmConfig.userAgentValue = context.packageName
            osmConfig.osmdroidBasePath = File(context.filesDir, "osmdroid")
            osmConfig.osmdroidTileCache = File(context.filesDir, "osmdroid/tiles")
            osmConfig.cacheMapTileCount = 400
            osmConfig.tileFileSystemCacheMaxBytes = 1024L * 1024L * 400L
            osmConfig.tileFileSystemCacheTrimBytes = 1024L * 1024L * 400L
            osmConfig.tileDownloadThreads = 2
            osmConfig.tileFileSystemThreads = 2
        }
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            minZoomLevel = 6.0
            maxZoomLevel = 22.0
            controller.setZoom(12.0)
            controller.setCenter(GeoPoint(50.69, 7.128))
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            //setTilesScaledToDpi(true)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { mapView },
        update = { map ->

            Log.d("MapScreen", "Update")

            // Clear map und crate new
            map.overlays.clear()

            allAreas.forEach { areaWithCoordinates ->
                if (areaWithCoordinates.coordinates.isNotEmpty()) {
                    val polygon = Polygon(map).apply {
                        val geoPoints = areaWithCoordinates.coordinates.sortedBy { it.orderIndex }
                            .map { coordinate -> GeoPoint(coordinate.latitude, coordinate.longitude) }

                        setPoints(geoPoints)
                        isEnabled = true

                        // Farbe wie gehabt
                        try {
                            val baseColor = try {
                                areaWithCoordinates.area.color.toColorInt()
                            } catch (_: IllegalArgumentException) {
                                android.graphics.Color.WHITE
                            }

                            fillPaint.color = android.graphics.Color.argb(
                                (0.3f * 255).toInt(),
                                android.graphics.Color.red(baseColor),
                                android.graphics.Color.green(baseColor),
                                android.graphics.Color.blue(baseColor)
                            )

                            outlinePaint.color = android.graphics.Color.argb(
                                (0.7f * 255).toInt(),
                                android.graphics.Color.red(baseColor),
                                android.graphics.Color.green(baseColor),
                                android.graphics.Color.blue(baseColor)
                            )
                        } catch (_: Exception) {
                            fillPaint.color = android.graphics.Color.argb(80, 255, 0, 0)
                            outlinePaint.color = android.graphics.Color.argb(180, 255, 0, 0)
                        }

                        outlinePaint.strokeWidth = 3f

                        // Setze InfoWindow mit onOpen-Logik
                        infoWindow = object : InfoWindow(R.layout.user_info_window, mapView) {
                            override fun onOpen(item: Any?) {
                                val view = mView
                                val areaTitleText = view.findViewById<TextView>(R.id.user_info_title)
                                val areaDescriptionText = view.findViewById<TextView>(R.id.user_info_description)
                                val areaInSqMeters = calculatePolygonArea(points)
                                val areaInHectares = areaInSqMeters / 10_000.0
                                val uploadedStatus = if (areaWithCoordinates.area.uploadedToServer) "✅ Hochgeladen" else "❌ Nicht hochgeladen"
                                areaTitleText.text = areaWithCoordinates.area.title.ifBlank { "Unbenannte Fläche" }
                                areaDescriptionText.text = """
                                    ${areaWithCoordinates.area.desc.ifBlank { "–" }}
                                    ${"%,.0f".format(Locale.getDefault(), areaInSqMeters)} m² (${String.format(Locale.getDefault(), "%.2f", areaInHectares)} ha)
                                    $uploadedStatus
                                    """.trimIndent()





                            }

                            override fun onClose() {}
                        }

                        setOnClickListener { polygon, mapView, eventPos ->
                            InfoWindow.closeAllInfoWindowsOn(mapView)
                            polygon.showInfoWindow()
                            true
                        }
                    }
                    map.overlays.add(polygon)
                }
            }

            // Area and marker save, if in DrawMode
            val areaElements = if (drawAreaMode) {
                (listOf(areaPolygon) + areaCornerMarkers)
            } else emptyList()

            // Add area and marker if drawAreaMode true
            if (drawAreaMode) {
                map.overlays.addAll(areaElements)
            }



            map.invalidate()

            if (!drawAreaMode) {

                // My Track with Segmentation
                val mySegments = mutableListOf<List<GeoPoint>>()
                if (myLocations.size > 1) {
                    var currentSegment = mutableListOf<GeoPoint>()
                    var lastLocation = myLocations.first()
                    var lastPoint = GeoPoint(lastLocation.latitude, lastLocation.longitude)
                    currentSegment.add(lastPoint)

                    for (i in 1 until myLocations.size) {
                        val location = myLocations[i]
                        val currentPoint = GeoPoint(location.latitude, location.longitude)

                        val timeDiff = location.timestamp - lastLocation.timestamp
                        val distance = lastPoint.distanceToAsDouble(currentPoint)

                        if (timeDiff > timeDiffMillis || distance > distanceMeters) {
                            // new section
                            if (currentSegment.size > 1) {
                                mySegments.add(currentSegment)
                            }
                            currentSegment = mutableListOf(currentPoint)
                        } else {
                            currentSegment.add(currentPoint)
                        }

                        lastPoint = currentPoint
                        lastLocation = location
                    }

                    // add last section
                    if (currentSegment.size > 1) {
                        mySegments.add(currentSegment)
                    }

                    // draw all sections
                    mySegments.forEach { segment ->
                        val polyline = Polyline(map).apply {
                            setPoints(segment)
                            outlinePaint.color = myTrackColor.toColorInt()
                            outlinePaint.strokeWidth = 6f
                            outlinePaint.isAntiAlias = true
                            isEnabled = true
                            infoWindow = null
                        }
                        map.overlays.add(polyline)
                    }
                }

                // Marker at last point of my track
                if (myGeoPoints.isNotEmpty()) {
                    val lastPoint = myGeoPoints.last()
                    val myLastLocation = myLocations.lastOrNull()

                    if (myLastLocation != null) {

                        val accuracyCircle = Polygon().apply {
                            points = Polygon.pointsAsCircle(lastPoint, myLastLocation.accuracy.toDouble())
                            fillPaint.color = ("#" + "22" + myTrackColor.removePrefix("#").drop(2)).toColorInt()
                            outlinePaint.color = ("#" + "33" + myTrackColor.removePrefix("#").drop(2)).toColorInt()
                            outlinePaint.strokeWidth = 2f
                        }

                        map.overlays.add(accuracyCircle)

                        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                        val timeString = dateFormat.format(Date(myLastLocation.timestamp))

                        val marker = Marker(map).apply {
                            isEnabled = true
                            position = lastPoint
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            icon = getColoredVectorMarker(
                                context,
                                R.drawable.location_pin,
                                myTrackColor.toColorInt()
                            )
                            infoWindow = object : InfoWindow(R.layout.user_info_window, map) {
                                @SuppressLint("SetTextI18n")
                                override fun onOpen(item: Any?) {
                                    val view = mView
                                    val areaNameText =
                                        view.findViewById<TextView>(R.id.user_info_title)
                                    areaNameText.text = myUserName
                                    val userDescriptionText =
                                        view.findViewById<TextView>(R.id.user_info_description)
                                    userDescriptionText.text = """
                                                $radioCallNameText: $radioCallName
                                                
                                                $lastPointText:
                                                ${formatLatitude(myLastLocation.latitude)}
                                                ${formatLongitude(myLastLocation.longitude)}
                                                $uTMMGRSText: ${locationToMGRSConverter.convert(lastPoint.latitude, lastPoint.longitude)}  
                                                $accuracyText: ${formatAccuracy(myLastLocation.accuracy)}
                                                $timeText: $timeString
                                            """.trimIndent()
                                }

                                override fun onClose() {
                                }
                            }
                            setOnMarkerClickListener { marker, mapView ->
                                if (marker.infoWindow?.isOpen == true) {
                                    marker.closeInfoWindow()
                                } else {
                                    InfoWindow.closeAllInfoWindowsOn(mapView)
                                    marker.showInfoWindow()
                                }
                                true
                            }


                        }
                        map.overlays.add(marker)

                    }
                }

                // Polyline for all user tracks without own track and marker, cut lines with time or distance differences
                if (securityLevel > 1) {
                    val grouped = allLocations.groupBy { it.userId }

                    grouped.forEach { (userId, locations) ->
                        if (locations.size > 1) {
                            val user = allUsers.find { it.id == userId }
                            val color = try {
                                user?.trackColor?.toColorInt() ?: android.graphics.Color.GRAY
                            } catch (_: Exception) {
                                android.graphics.Color.GRAY
                            }

                            val segments = mutableListOf<List<GeoPoint>>()
                            var currentSegment = mutableListOf<GeoPoint>()
                            var lastTimestamp = locations.first().timestamp
                            var lastPoint = GeoPoint(locations.first().latitude, locations.first().longitude)
                            currentSegment.add(lastPoint)

                            for (i in 1 until locations.size) {
                                val location = locations[i]
                                val currentPoint = GeoPoint(location.latitude, location.longitude)
                                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                val currentDate = formatter.parse(location.timestamp)
                                val lastDate = formatter.parse(lastTimestamp)
                                val timeDiff = if (currentDate != null && lastDate != null) {
                                    currentDate.time - lastDate.time
                                } else {
                                    Log.d("MapScreen","Error while time diff")
                                    0L
                                }
                                val distance = lastPoint.distanceToAsDouble(currentPoint)

                                if (timeDiff > timeDiffMillis || distance > distanceMeters) {
                                    // Start a new segment
                                    if (currentSegment.size > 1) {
                                        segments.add(currentSegment)
                                    }
                                    currentSegment = mutableListOf(currentPoint)
                                } else {
                                    currentSegment.add(currentPoint)
                                }

                                lastPoint = currentPoint
                                lastTimestamp = location.timestamp
                            }
                            // Add last segment
                            if (currentSegment.size > 1) {
                                segments.add(currentSegment)
                            }

                            // Draw segments
                            segments.forEach { segment ->
                                val polyline = Polyline(map).apply {
                                    setPoints(segment)
                                    outlinePaint.color = color
                                    outlinePaint.strokeWidth = 5f
                                    isVisible = true
                                    isEnabled = true
                                    infoWindow = null
                                }
                                map.overlays.add(polyline)
                            }

                            // Marker for the last point in the full track
                            val last = locations.last()
                            val marker = Marker(map).apply {
                                isEnabled = true
                                position = GeoPoint(last.latitude, last.longitude)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                icon = getColoredVectorMarker(context, R.drawable.location_pin, color)

                                infoWindow = object : InfoWindow(R.layout.user_info_window, map) {
                                    @SuppressLint("SetTextI18n")
                                    override fun onOpen(item: Any?) {
                                        val view = mView
                                        val areaNameText =
                                            view.findViewById<TextView>(R.id.user_info_title)
                                        areaNameText.text = user?.username ?: unknownText
                                        val areaDescriptionText =
                                            view.findViewById<TextView>(R.id.user_info_description)
                                        areaDescriptionText.text = """
                                                        $radioCallNameText: ${user?.radiocallname ?: "-"}
                                                        
                                                        $lastPointText:
                                                        ${formatLatitude(last.latitude)}
                                                        ${formatLongitude(last.longitude)}
                                                        $uTMMGRSText: ${locationToMGRSConverter.convert(last.latitude, last.longitude)}
                                                        $accuracyText: ${formatAccuracy(last.accuracy.toFloat())}
                                                        $timeText: ${last.timestamp}
                                                    """.trimIndent()
                                    }

                                    override fun onClose() {
                                    }
                                }
                                setOnMarkerClickListener { marker, mapView ->
                                    if (marker.infoWindow?.isOpen == true) {
                                        marker.closeInfoWindow()
                                    } else {
                                        InfoWindow.closeAllInfoWindowsOn(mapView)
                                        marker.showInfoWindow()
                                    }
                                    true
                                }
                            }
                            map.overlays.add(marker)
                        }
                    }
                }

                // Center to my position
                if (!mapCenteredOnce && map.width > 0 && map.height > 0) {
                    val allPoints = (myGeoPoints + allLocations.map { GeoPoint(it.latitude, it.longitude) })
                    if (allPoints.isNotEmpty()) {
                        val boundingBox = BoundingBox.fromGeoPointsSafe(allPoints)
                        map.zoomToBoundingBox(boundingBox, true, 100)
                        markAsCentered()
                    }

                }

            }

            map.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    InfoWindow.closeAllInfoWindowsOn(mapView)
                }


                if (drawAreaMode && event.action == MotionEvent.ACTION_UP) {
                    val projectionArea = map.projection
                    val geoPointArea = projectionArea.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                    areaPoints.add(geoPointArea)

                    if (areaPoints.size >= 3) {
                        areaPolygon.setPoints(areaPoints.toList())
                        if (!map.overlays.contains(areaPolygon)) {
                            map.overlays.add(areaPolygon)
                        }
                    }

                    // Marker for last Point

                    val cornerMarker = Marker(map).apply {
                        position = geoPointArea
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = AppCompatResources.getDrawable(context, R.drawable.circle_small)
                        isDraggable = false
                        infoWindow = null
                    }
                    areaCornerMarkers.add(cornerMarker)
                    map.overlays.add(cornerMarker)

                    // Update Area
                    if (areaPoints.size >= 3) {
                        areaPolygon.setPoints(areaPoints.toList())
                        if (!map.overlays.contains(areaPolygon)) {
                            map.overlays.add(areaPolygon)
                        }
                    }
                    map.invalidate()
                }
                false
            }

            if (!map.overlays.contains(scaleBarOverlay)) {
                map.overlays.add(scaleBarOverlay)
            }

        }
    ).also {
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    else -> {}
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }
    LaunchedEffect(drawAreaMode) {
        if (!drawAreaMode) {
            areaPoints.clear()
            areaPolygon.setPoints(emptyList())
            mapView.overlays.remove(areaPolygon)

            areaCornerMarkers.forEach { marker ->
                mapView.overlays.remove(marker)
            }
            areaCornerMarkers.clear()

            mapView.invalidate()
            Log.d("MapViewContainer", "DrawArea cleared via drawAreaMode switch")
        }
    }
    LaunchedEffect(drawAreaMode) {
        if (!drawAreaMode) {
            areaPoints.clear()
            areaPolygon.setPoints(emptyList())
            areaCornerMarkers.clear()
            Log.d("MapViewContainer", "Area editing cleared after exiting draw mode.")
        }
    }
}