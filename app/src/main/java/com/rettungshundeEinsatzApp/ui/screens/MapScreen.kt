package com.rettungshundeEinsatzApp.ui.screens

import android.annotation.SuppressLint
import android.app.Service.MODE_PRIVATE
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rettungshundeEinsatzApp.R
import com.rettungshundeEinsatzApp.database.mylocallocation.MyLocationDatabase
import com.rettungshundeEinsatzApp.service.myLocation.MyLocationLatLongToMGRS
import com.rettungshundeEinsatzApp.ui.ReaAppTheme
import com.rettungshundeEinsatzApp.viewmodel.mapscreen.MapScreenMyTrackViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Texture
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rettungshundeEinsatzApp.activity.ContactActivity
import com.rettungshundeEinsatzApp.activity.ManageUsersOverviewActivity
import com.rettungshundeEinsatzApp.activity.NewUserActivity
import com.rettungshundeEinsatzApp.activity.SettingsActivity
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataProvider
import com.rettungshundeEinsatzApp.database.areas.AreaDatabase
import com.rettungshundeEinsatzApp.database.areas.SavedArea
import com.rettungshundeEinsatzApp.database.areas.UploadStatus
import com.rettungshundeEinsatzApp.functions.calculatePolygonArea
import com.rettungshundeEinsatzApp.functions.deleteAllGPSData
import com.rettungshundeEinsatzApp.functions.deleteMyGPSData
import com.rettungshundeEinsatzApp.functions.downloadAllGpsLocations
import com.rettungshundeEinsatzApp.functions.downloadAllUserData
import com.rettungshundeEinsatzApp.service.myLocation.MyLocationStatus
import com.rettungshundeEinsatzApp.viewmodel.location.MapScreenAllTracksViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rettungshundeEinsatzApp.functions.deleteAllAreas
import com.rettungshundeEinsatzApp.functions.downloadAreas
import com.rettungshundeEinsatzApp.functions.uploadAreas
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.infowindow.InfoWindow
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle
import androidx.core.net.toUri
import org.osmdroid.util.BoundingBox
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.edit
import com.rettungshundeEinsatzApp.activity.ReportActivity
import org.osmdroid.tileprovider.tilesource.XYTileSource

@SuppressLint("ClickableViewAccessibility")
@Composable
fun MapScreen(onStartGPS: () -> Unit, onStopGPS: () -> Unit){

    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("REAPrefs", MODE_PRIVATE)
    val myTrackColor = sharedPreferences.getString("myTrackColor", "#FF0000FF") ?: "#FF0000FF"
    var gpsIsRunningPref by remember { mutableStateOf(sharedPreferences.getBoolean("gpsRunning", false)) }
    val token: String = sharedPreferences.getString("token", "").toString()
    val serverApiURL = sharedPreferences.getString("serverApiURL", "").toString()
    val myUserName = sharedPreferences.getString("username", "").toString()
    val radioCallName = sharedPreferences.getString("radiocallname", "").toString()
    val securityLevel = sharedPreferences.getString("securityLevel", "1")?.toIntOrNull() ?: 1
    val myLocationDatabase = MyLocationDatabase.getDatabase(context)
    val viewModel: MapScreenMyTrackViewModel = viewModel(factory = MapScreenMyTrackViewModel.Factory(myLocationDatabase.locationDao()))
    val myLocations by viewModel.locations.collectAsState()
    val locationToMGRSConverter = MyLocationLatLongToMGRS()
    var dialogInfoAndConfirm by remember { mutableStateOf(false) }
    var dialogMod by remember { mutableIntStateOf(0) }
    var dialogIsSubmitting by remember { mutableStateOf(false) }
    var dialogShowResult by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    var resultSuccess by remember { mutableStateOf(false) }
    val db = AllUserDataProvider.getDatabase(context)
    val locationDao = db.allUsersLocationsDao()
    val userDao = db.allUserDataDao()
    val mapCenteredOnce by viewModel.mapCenteredOnce
    val areaCornerMarkers = remember { mutableStateListOf<Marker>() }
    val areaDatabase = AreaDatabase.getDatabase(context)
    val areaDao = areaDatabase.areaDao()
    var areaName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color.Red) }
    var dialogShowSaveArea by remember { mutableStateOf(false) }
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val lifecycleOwner = LocalLifecycleOwner.current
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val notUploadedAreas by areaDao.getByStatus(UploadStatus.NOT_UPLOADED)
        .collectAsState(initial = emptyList())
    val receivedAreas by areaDao.getByStatus(UploadStatus.RECEIVED)
        .collectAsState(initial = emptyList())
    val dialogInfoAndConfirmTitle = when (dialogMod) {
        1 -> stringResource(id = R.string.dialog_delete_my_GPS_data_title)
        2 -> stringResource(id = R.string.dialog_delete_all_GPS_data_title)
        3 -> stringResource(id = R.string.dialog_delete_all_areas_title)
        else -> stringResource(id = R.string.dialog_invalid_input)
    }
    val dialogInfoAndConfirmText = when (dialogMod) {
        1 -> stringResource(id = R.string.dialog_delete_my_GPS_data_text)
        2 -> stringResource(id = R.string.dialog_delete_all_GPS_data_text)
        3 -> stringResource(id = R.string.dialog_delete_all_areas_text)
        else -> stringResource(id = R.string.dialog_invalid_input)
    }


    val lastPointText = stringResource(id = R.string.last_point)
    val radioCallNameText = stringResource(id = R.string.radio_call_name)
    val accuracyText = stringResource(id = R.string.accuracy)
    val uTMMGRSText = stringResource(id = R.string.utm_mgrs)
    val timeText = stringResource(id = R.string.time)
    val unknownText = stringResource(id = R.string.unknown)
    val oSMCopyright = stringResource(id = R.string.osm_copyright)




    val viewModelAllTracks: MapScreenAllTracksViewModel = viewModel(
        factory = MapScreenAllTracksViewModel.Factory(locationDao, userDao)
    )

    val allLocations by viewModelAllTracks.allLocations.collectAsState()
    val allUsers by viewModelAllTracks.allUsers.collectAsState()


    val myGeoPoints = myLocations.map { GeoPoint(it.latitude, it.longitude) }
    val gpsActive by MyLocationStatus.gpsActive.collectAsStateWithLifecycle()
    var drawAreaMode by remember { mutableStateOf(false) }
    val areaPoints = remember { mutableStateListOf<GeoPoint>() }
    val areaPolygon = remember {
        Polygon().apply {
            fillPaint.color = android.graphics.Color.argb(80, 255, 0, 0)
            outlinePaint.color = android.graphics.Color.RED
            outlinePaint.strokeWidth = 4f
        }
    }


    val openTopoMapSource = XYTileSource(
        "OpenTopoMap",
        1, 17,
        256,
        ".png",
        arrayOf("https://tile.opentopomap.org/"),
        "© OpenTopoMap (CC-BY-SA)"
    )

    val configuration = LocalConfiguration.current
    val mapView = remember(configuration.orientation) {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            minZoomLevel = 6.0
            maxZoomLevel = 19.0
            controller.setZoom(12.0)
            controller.setCenter(GeoPoint(50.69, 7.128))
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            onResume()
        }
    }

    Configuration.getInstance().userAgentValue = context.packageName
    Configuration.getInstance().tileFileSystemCacheMaxBytes = 1024L * 1024L * 100 // 100 MB
    Configuration.getInstance().osmdroidBasePath = File(context.filesDir, "osmdroid")
    Configuration.getInstance().osmdroidTileCache = File(context.filesDir, "osmdroid/tiles")
    Configuration.getInstance().tileDownloadThreads = 2
    Configuration.getInstance().tileFileSystemThreads = 2
    Configuration.getInstance().cacheMapTileCount = 400

    var menuVisible by remember { mutableStateOf(false) }
    BackHandler(enabled = menuVisible) {
        menuVisible = false
    }

    val statusBarPaddingPx = with(LocalDensity.current) {
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding().toPx()
    }

    val scaleBarOverlay = remember(statusBarPaddingPx) {
        ScaleBarOverlay(mapView).apply {
            isEnabled = true
            setAlignBottom(false)
            setAlignRight(false)
            setScaleBarOffset(20, (statusBarPaddingPx + 20f).toInt())
            barPaint.strokeWidth = 4f
            barPaint.color = android.graphics.Color.BLACK
            textPaint.color = android.graphics.Color.BLACK
            textPaint.textSize = 35f
        }
    }

    val annotatedLinkText = buildAnnotatedString {
        append("© ")
        pushStringAnnotation(tag = "URL", annotation = "https://www.openstreetmap.org/copyright")
        withStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(oSMCopyright)
        }
        pop()
    }

    val timeDiffMillis = 1 * 60 * 1000 // 1 minutes
    val distanceMeters = 100.0

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "gpsRunning") {
                gpsIsRunningPref = sharedPreferences.getBoolean("gpsRunning", false)
            }
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        onDispose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }



    ReaAppTheme {
        Box(modifier = Modifier.fillMaxSize()) {

            // background for statusBar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statusBarPadding)
                    .background(Color.Black.copy(alpha = 0.3f))
                    .align(Alignment.TopCenter)
                    .zIndex(2f)
            )

            // Overlay-Box for nav bar at bottom of android
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(navBarPadding)
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.3f))
                    .zIndex(2f)
            )

            // Attribution OpenStreetMap
            Text(
                text = annotatedLinkText,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = navBarPadding + 0.dp)
                    .zIndex(3f)
                    .clickable {
                        val url = annotatedLinkText
                            .getStringAnnotations(tag = "URL", start = 0, end = annotatedLinkText.length)
                            .firstOrNull()?.item
                        url?.let {
                            val intent = Intent(Intent.ACTION_VIEW, it.toUri())
                            context.startActivity(intent)
                        }
                    },
                fontSize = 12.sp,
                color = Color.Black
            )

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { mapView },
                update = { map ->

                    Log.d("MapScreen", "Update")

                    // Clear map und crate new
                    map.overlays.clear()

                    // Area and marker save, if in DrawMode
                    val areaElements = if (drawAreaMode) {
                        (listOf(areaPolygon) + areaCornerMarkers)
                    } else emptyList()

                    // Add area and marker if drawAreaMode true
                    if (drawAreaMode) {
                        map.overlays.addAll(areaElements)
                    }

                    receivedAreas.forEach { savedArea ->
                        val points = savedArea.points.split(";").mapNotNull {
                            val latLon = it.split(",")
                            if (latLon.size == 2) {
                                val lat = latLon[0].toDoubleOrNull()
                                val lon = latLon[1].toDoubleOrNull()
                                if (lat != null && lon != null) GeoPoint(lat, lon) else null
                            } else null
                        }

                        if (points.size >= 3) {
                            // Get color from String
                            val outlineColor = try {
                                savedArea.color.toColorInt()
                            } catch (e: Exception) {
                                android.graphics.Color.GRAY
                            }

                            val fillColor = colorWithAlpha(outlineColor, 30)
                            val areaM2 = calculatePolygonArea(points)
                            val polygon = Polygon(map).apply {
                                setPoints(points)
                                fillPaint.color = fillColor
                                outlinePaint.color = outlineColor
                                outlinePaint.strokeWidth = 4f
                                isEnabled = true
                                isVisible = true
                                infoWindow = null
                                infoWindow = object : InfoWindow(R.layout.area_info_window, map) {
                                    override fun onOpen(item: Any?) {
                                        val view = mView
                                        val areaNameText =
                                            view.findViewById<TextView>(R.id.area_title)
                                        areaNameText.text = savedArea.name
                                        val description = context.getString(R.string.area_description, areaM2)
                                        val areaDescriptionText =
                                            view.findViewById<TextView>(R.id.area_description)
                                        areaDescriptionText.text = description
                                    }

                                    override fun onClose() {
                                    }
                                }
                                setOnClickListener { _, _, _ ->
                                    if (this.infoWindow?.isOpen == true) {
                                        this.closeInfoWindow()
                                    } else {
                                        InfoWindow.closeAllInfoWindowsOn(mapView)
                                        this.showInfoWindow()
                                    }
                                    true
                                }
                            }
                            map.overlays.add(polygon)
                        }
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
                                    fillPaint.color = ("#" + "22" + myTrackColor.removePrefix("#").drop(2)).toColorInt()  // halbtransparente farbe bei 20% mit alphawert 33
                                    outlinePaint.color = ("#" + "33" + myTrackColor.removePrefix("#").drop(2)).toColorInt()
                                    outlinePaint.strokeWidth = 2f
                                }

                                map.overlays.add(accuracyCircle)

                                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                                val timeString = dateFormat.format(Date(myLastLocation.timestamp))

                                val marker = Marker(map).apply {
                                    isEnabled = !drawAreaMode
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
                                            val areaDescriptionText =
                                                view.findViewById<TextView>(R.id.user_info_description)
                                            areaDescriptionText.text = """
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
                                    } catch (e: Exception) {
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
                                            isVisible = !drawAreaMode
                                            isEnabled = !drawAreaMode
                                            infoWindow = null
                                        }
                                        map.overlays.add(polyline)
                                    }

                                    // Marker for the last point in the full track
                                    val last = locations.last()
                                    val marker = Marker(map).apply {
                                        isEnabled = !drawAreaMode
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

                            notUploadedAreas.forEach { savedArea ->
                                val points = savedArea.points.split(";").mapNotNull {
                                    val latLon = it.split(",")
                                    if (latLon.size == 2) {
                                        val lat = latLon[0].toDoubleOrNull()
                                        val lon = latLon[1].toDoubleOrNull()
                                        if (lat != null && lon != null) GeoPoint(lat, lon) else null
                                    } else null
                                }

                                if (points.size >= 3) {
                                    // Get Color from String
                                    val outlineColor = try {
                                        savedArea.color.toColorInt()
                                    } catch (e: Exception) {
                                        android.graphics.Color.GRAY
                                    }

                                    val fillColor = colorWithAlpha(outlineColor, 30)
                                    val areaM2 = calculatePolygonArea(points)
                                    val polygon = Polygon(map).apply {
                                        setPoints(points)
                                        fillPaint.color = fillColor
                                        outlinePaint.color = outlineColor
                                        outlinePaint.strokeWidth = 4f
                                        isEnabled = true
                                        isVisible = true
                                        infoWindow = null
                                        infoWindow = object : InfoWindow(R.layout.area_info_window, map) {
                                            override fun onOpen(item: Any?) {
                                                val view = mView
                                                val areaNameText =
                                                    view.findViewById<TextView>(R.id.area_title)
                                                areaNameText.text = savedArea.name
                                                val description = context.getString(R.string.area_description, areaM2)
                                                val areaDescriptionText =
                                                    view.findViewById<TextView>(R.id.area_description)
                                                areaDescriptionText.text = description
                                            }

                                            override fun onClose() {
                                            }
                                        }
                                        setOnClickListener { _, _, _ ->
                                            if (this.infoWindow?.isOpen == true) {
                                                this.closeInfoWindow()
                                            } else {
                                                InfoWindow.closeAllInfoWindowsOn(mapView)
                                                this.showInfoWindow()
                                            }
                                            true
                                        }
                                    }

                                    val centerPoint = getPolygonCenter(points)
                                    val centerMarker = Marker(mapView).apply {
                                        position = centerPoint
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                        icon = AppCompatResources.getDrawable(context, R.drawable.red_cross)
                                        isDraggable = false
                                        isEnabled = true
                                        infoWindow = null
                                    }

                                    map.overlays.add(polygon)
                                    map.overlays.add(centerMarker)
                                }
                            }
                        }

                        // Center to my position
                        if (!mapCenteredOnce && map.width > 0 && map.height > 0) {
                            val allPoints = (myGeoPoints + allLocations.map { GeoPoint(it.latitude, it.longitude) })
                            if (allPoints.isNotEmpty()) {
                                val boundingBox = BoundingBox.fromGeoPointsSafe(allPoints)
                                map.zoomToBoundingBox(boundingBox, true, 100)
                                viewModel.markAsCentered()
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

                            if (drawAreaMode && areaPoints.size >= 3) {
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
                            if (drawAreaMode && areaPoints.size >= 3) {
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


            if (!drawAreaMode) {

                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 10.dp, bottom = navBarPadding + 30.dp, end = 16.dp)
                        .align(Alignment.BottomEnd)
                ) {

                    if (securityLevel > 1) {
                        FloatingActionButton(
                            onClick = {
                                InfoWindow.closeAllInfoWindowsOn(mapView)
                                menuVisible = false
                                drawAreaMode = !drawAreaMode

                                if (!drawAreaMode) {
                                    areaPoints.clear()
                                    mapView.overlays.remove(areaPolygon)
                                    mapView.invalidate()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Texture,
                                contentDescription = stringResource(id = R.string.add_area)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    FloatingActionButton(
                        onClick = {
                            viewModel.resetCentering()
                            if (securityLevel > 1) {

                                // Get All user Data to Refresh Database
                                downloadAllUserData(token, serverApiURL, db.allUserDataDao()) { msg ->
                                    val parts = msg.split(",")
                                    Log.d("MapScreen","Refresh Button pushed: GetAllUserData: status=${parts[0]}, message=${parts[1]}"
                                    )

                                    downloadAllGpsLocations(
                                        token,
                                        serverApiURL,
                                        db.allUsersLocationsDao()
                                    ) { msg2 ->
                                        val parts2 = msg2.split(",", limit = 2)
                                        Log.d("MapActivity", "Refresh Button pushed: GetAllGpsLocations: status=${parts2[0]}, message=${
                                                parts2.getOrNull(
                                                    1
                                                ) ?: ""
                                            }"
                                        )
                                    }
                                }

                                // Push not uploaded Areas to server
                                uploadAreas(token, serverApiURL, areaDao) { msg ->
                                    val parts = msg.split(",")
                                    Log.d(
                                        "MapScreen",
                                        "Refresh Button pushed: PushAreas: status=${parts[0]}, message=${parts[1]}"
                                    )

                                    // Get uploaded Areas from server
                                    downloadAreas(token, serverApiURL, areaDao) { msg2 ->
                                        val parts2 = msg2.split(",")
                                        Log.d(
                                            "MapScreen",
                                            "Refresh Button pushed: GetAreas: status=${parts2[0]}, message=${parts2[1]}"
                                        )
                                    }
                                }

                            }else{
                                // Push not uploaded Areas to server
                                uploadAreas(token, serverApiURL, areaDao) { msg ->
                                    val parts = msg.split(",")
                                    Log.d(
                                        "MapScreen",
                                        "Refresh Button pushed: PushAreas: status=${parts[0]}, message=${parts[1]}"
                                    )

                                    // Get uploaded Areas from server
                                    downloadAreas(token, serverApiURL, areaDao) { msg2 ->
                                        val parts2 = msg2.split(",")
                                        Log.d(
                                            "MapScreen",
                                            "Refresh Button pushed: GetAreas: status=${parts2[0]}, message=${parts2[1]}"
                                        )

                                    }

                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(id = R.string.refresh)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    FloatingActionButton(
                        onClick = {
                            if (gpsActive || gpsIsRunningPref) {
                                onStopGPS()
                                sharedPreferences.edit{ putBoolean("gpsRunning", false) }
                                Log.d("REA HomeScreen", "GPS Stop pushed")
                            } else {
                                onStartGPS()
                                Log.d("REA HomeScreen", "GPS Start pushed")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (gpsActive || gpsIsRunningPref) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (gpsActive) stringResource(id = R.string.stop_gps_tracking) else stringResource(id = R.string.start_gps_tracking)
                        )
                    }


                }

                FloatingActionButton(
                    onClick = { menuVisible = !menuVisible },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, top = 10.dp, bottom = navBarPadding + 30.dp, end = 16.dp)
                        .zIndex(1f)
                ) {
                    Icon(Icons.Default.Menu, contentDescription = stringResource(id = R.string.open_menu))
                }

            }else if ( securityLevel > 1){
                Row(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 40.dp, bottom = 16.dp, end = 16.dp)
                        .align(Alignment.TopEnd)
                ) {

                    FloatingActionButton(
                        onClick = {
                            dialogShowSaveArea = true
                                  },
                        modifier = Modifier
                            .padding(start = 0.dp, top = 0.dp, bottom = 0.dp, end = 0.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = stringResource(id = R.string.save))
                    }

                    Spacer(modifier = Modifier.width(16.dp))


                    FloatingActionButton(
                        onClick = {
                            drawAreaMode = !drawAreaMode
                            if (!drawAreaMode) {
                                areaPoints.clear()
                                areaPolygon.setPoints(emptyList())
                                mapView.overlays.remove(areaPolygon)
                                areaCornerMarkers.forEach { mapView.overlays.remove(it) }
                                areaCornerMarkers.clear()
                                mapView.invalidate()
                            }
                        },
                        modifier = Modifier
                            .padding(start = 0.dp, top = 0.dp, bottom = 0.dp, end = 0.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = stringResource(id = R.string.cancel))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    FloatingActionButton(
                        onClick = {
                            if (areaPoints.isNotEmpty()) {
                                areaPoints.removeAt(areaPoints.lastIndex)
                                areaPolygon.setPoints(areaPoints.toList())
                                val marker = areaCornerMarkers.removeLastOrNull()
                                if (marker != null) mapView.overlays.remove(marker)
                            }
                            mapView.invalidate()
                        },
                        modifier = Modifier
                            .padding(start = 0.dp, top = 0.dp, bottom = 0.dp, end = 0.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = stringResource(id = R.string.undo))
                    }
                }

                if (drawAreaMode && areaPoints.size >= 3) {
                    val areaM2 = calculatePolygonArea(areaPoints)
                    Text(
                        text = "${stringResource(id = R.string.area)}: %.1f m²".format(areaM2),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                            .padding(8.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            var longitude = "--"
            var latitude = ""
            var utm = "--"
            var accuracy = "--"
            var lastGPSTime = "--"

            if (myGeoPoints.isNotEmpty()) {
                val lastPoint = myGeoPoints.last()
                val myLastLocation = myLocations.lastOrNull()
                latitude = formatLatitude(lastPoint.latitude)
                longitude = formatLongitude(lastPoint.longitude)
                utm = locationToMGRSConverter.convert(lastPoint.latitude, lastPoint.longitude)
                if (myLastLocation != null) {
                    accuracy = formatAccuracy(myLastLocation.accuracy)
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                    lastGPSTime = dateFormat.format(Date(myLastLocation.timestamp))
                }
        }

            // Menu
            AnimatedVisibility(
                visible = menuVisible,
                enter = slideInHorizontally(initialOffsetX = { -it }),
                exit = slideOutHorizontally(targetOffsetX = { -it }),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(250.dp)
                    .align(Alignment.TopStart)
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                    .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        stringResource(id = R.string.my_position),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(id = R.string.geographic_coordinates),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(latitude,
                        color = MaterialTheme.colorScheme.onBackground)
                    Text(longitude,
                        color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        stringResource(id = R.string.utm_mgrs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(utm,
                        color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        stringResource(id = R.string.accuracy),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(accuracy,
                        color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        stringResource(id = R.string.last_update),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(lastGPSTime,
                        color = MaterialTheme.colorScheme.onBackground)

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        "Menü",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (gpsActive || gpsIsRunningPref) {
                                onStopGPS()
                                Log.d("REA HomeScreen", "GPS Stop pushed")
                            } else {
                                onStartGPS()
                                Log.d("REA HomeScreen", "GPS Start pushed")
                            }
                        },
                        enabled = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShareLocation,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (gpsActive) "GPS Stop" else "GPS Start"
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val intent = Intent(context, ContactActivity::class.java)
                            context.startActivity(intent)
                        },
                        enabled = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Contacts,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.contacts))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            dialogMod = 1
                            dialogInfoAndConfirm = true
                        },
                        enabled = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.delete_my_gps_data))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val intent = Intent(context, ReportActivity::class.java)
                            context.startActivity(intent)
                        },
                        enabled = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Create,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.write_operational_report))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (securityLevel > 1) {

                        Button(
                            onClick = {
                                dialogMod = 2
                                dialogInfoAndConfirm = true
                            },
                            enabled = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(id = R.string.delete_all_gps_data))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                dialogMod = 3
                                dialogInfoAndConfirm = true
                            },
                            enabled = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(id = R.string.delete_all_areas))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                    }

                    if (securityLevel > 2) {

                        Button(
                            onClick = {
                                val intent = Intent(context, ManageUsersOverviewActivity::class.java)
                                context.startActivity(intent)
                            },
                            enabled = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ManageAccounts,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(id = R.string.settings_manage_users))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val intent = Intent(context, NewUserActivity::class.java)
                                context.startActivity(intent)
                            },
                            enabled = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(id = R.string.settings_create_new_user))
                        }

                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val intent = Intent(context, SettingsActivity()::class.java)
                            context.startActivity(intent)
                        },
                        enabled = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.dashboard_bottom_menu_settings))
                    }

                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }

        if (dialogInfoAndConfirm) {
            AlertDialog(
                onDismissRequest = {},
                title = {Text(dialogInfoAndConfirmTitle)},
                text = {Text(dialogInfoAndConfirmText)},
                confirmButton = {
                    Button(onClick = {
                        dialogInfoAndConfirm = false
                        when (dialogMod) {
                            1 -> {
                                dialogIsSubmitting = true
                                deleteMyGPSData(context) { success, message ->
                                    resultMessage = message
                                    resultSuccess = success
                                    dialogIsSubmitting = false
                                    dialogShowResult = true
                                }
                            }

                            2 -> {
                                dialogIsSubmitting = true
                                deleteAllGPSData(serverApiURL, token, locationDao) { success, message ->
                                    resultMessage = message
                                    resultSuccess = success
                                    dialogIsSubmitting = false
                                    dialogShowResult = true
                                }
                            }

                            3 -> {
                                dialogIsSubmitting = true
                                dialogIsSubmitting = false
                                dialogShowResult = true
                                deleteAllAreas(token, serverApiURL, areaDao) { success, message ->
                                    resultMessage = message
                                    resultSuccess = success
                                    dialogIsSubmitting = false
                                    dialogShowResult = true
                                }
                            }

                            else -> {
                                // optional: Logging oder Fallback
                            }
                        }


                    }) {
                        Text(stringResource(id = R.string.confirm))
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = {
                        dialogMod = 0
                        dialogInfoAndConfirm = false
                    }) {
                        Text(stringResource(id = R.string.cancel))
                    }
                }
            )
        }

        if (dialogIsSubmitting) {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                title = { Text(stringResource(id = R.string.please_wait)) },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator()
                        Text(stringResource(id = R.string.in_progress_points))
                    }
                }
            )
        }

        if (dialogShowResult) {
            AlertDialog(
                onDismissRequest = { dialogShowResult = false },
                confirmButton = {
                    Button(onClick = {
                        dialogMod = 0
                        dialogShowResult = false
                    }) {
                        Text("OK")
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (resultSuccess) Icons.Filled.CheckCircle else Icons.Filled.Error,
                            contentDescription = null,
                            tint = if (resultSuccess) Color(0xFF4CAF50) else Color.Red
                        )

                        Text(
                            text = if (resultSuccess) stringResource(id = R.string.success) else stringResource(id = R.string.failed)
                        )
                    }
                },
                text = {
                    if(dialogMod == 1 && resultSuccess){
                        Text(stringResource(id = R.string.dialog_delete_my_GPS_data_result))
                    }else if(dialogMod == 2 && resultSuccess){
                        Text(stringResource(id = R.string.dialog_delete_all_GPS_data_result))
                    }else if(dialogMod == 3 && resultSuccess){
                        Text(stringResource(id = R.string.dialog_delete_all_areas_result))
                    }else {
                        Text("${stringResource(id = R.string.dialog_invalid_input_or_error)}: $resultMessage " )
                    }

                }
            )
        }
        if (dialogShowSaveArea) {
            AlertDialog(
                onDismissRequest = { dialogShowSaveArea = false },
                confirmButton = {
                    TextButton(onClick = {
                        val jsonPoints = areaPoints.joinToString(separator = ";") {
                            "${it.latitude},${it.longitude}"
                        }
                        val colorHex = String.format("#%06X", 0xFFFFFF and selectedColor.toArgb())

                        CoroutineScope(Dispatchers.IO).launch {
                            val newArea = SavedArea(
                                name = areaName.ifBlank { "Unnamed"},
                                timestamp = System.currentTimeMillis(),
                                points = jsonPoints,
                                color = colorHex,
                                uploadStatus = UploadStatus.NOT_UPLOADED
                            )
                            areaDao.insert(newArea)
                        }

                        areaPoints.clear()
                        areaPolygon.setPoints(emptyList())
                        areaCornerMarkers.forEach { mapView.overlays.remove(it) }
                        areaCornerMarkers.clear()
                        mapView.invalidate()

                        dialogShowSaveArea = false
                        areaName = ""
                        selectedColor = Color.Red
                        drawAreaMode = false
                    }) {
                        Text(stringResource(id = R.string.save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { dialogShowSaveArea = false }) {
                        Text(stringResource(id = R.string.cancel))
                    }
                },
                title = { Text(stringResource(id = R.string.save_area)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = areaName,
                            onValueChange = { areaName = it },
                            label = { Text(stringResource(id = R.string.name_of_area)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(stringResource(id = R.string.choose_color))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Gray
                            ).forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(color)
                                        .border(
                                            width = if (selectedColor == color) 3.dp else 1.dp,
                                            color = if (selectedColor == color) Color.Black else Color.DarkGray,
                                            shape = RoundedCornerShape(50)
                                        )
                                        .clickable { selectedColor = color }
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

fun formatLongitude(longitude: Double): String {
    val direction = if (longitude >= 0) "O" else "W"
    return "$direction ${"%.5f".format(kotlin.math.abs(longitude))}°"
}

fun formatLatitude(latitude: Double): String {
    val direction = if (latitude >= 0) "N" else "S"
    return "$direction ${"%.5f".format(kotlin.math.abs(latitude))}°"
}

fun formatAccuracy(latitude: Float): String {
    return "± ${latitude.toInt()} m"
}

fun getColoredVectorMarker(context: Context, drawableRes: Int, color: Int): Drawable {
    val drawable = ContextCompat.getDrawable(context, drawableRes)
        ?: throw IllegalArgumentException("loading Drawable error")
    val wrapped = DrawableCompat.wrap(drawable).mutate()
    DrawableCompat.setTint(wrapped, color)
    return wrapped
}

fun colorWithAlpha(color: Int, alpha: Int): Int {
    val r = android.graphics.Color.red(color)
    val g = android.graphics.Color.green(color)
    val b = android.graphics.Color.blue(color)
    return android.graphics.Color.argb(alpha, r, g, b)
}

fun getPolygonCenter(points: List<GeoPoint>): GeoPoint {
    val lat = points.map { it.latitude }.average()
    val lon = points.map { it.longitude }.average()
    return GeoPoint(lat, lon)
}
