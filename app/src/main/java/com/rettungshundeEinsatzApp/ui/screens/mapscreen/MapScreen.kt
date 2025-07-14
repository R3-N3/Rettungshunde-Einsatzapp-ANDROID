package com.rettungshundeEinsatzApp.ui.screens.mapscreen

import android.annotation.SuppressLint
import android.app.Service.MODE_PRIVATE
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rettungshundeEinsatzApp.R
import com.rettungshundeEinsatzApp.database.mylocallocation.MyLocationDatabase
import com.rettungshundeEinsatzApp.service.myLocation.MyLocationLatLongToMGRS
import com.rettungshundeEinsatzApp.ui.ReaAppTheme
import com.rettungshundeEinsatzApp.viewmodel.MyTrackViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rettungshundeEinsatzApp.activity.ContactActivity
import com.rettungshundeEinsatzApp.activity.ManageUsersOverviewActivity
import com.rettungshundeEinsatzApp.activity.SettingsActivity
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataProvider
import com.rettungshundeEinsatzApp.functions.calculatePolygonArea
import com.rettungshundeEinsatzApp.functions.downloadAllGpsLocations
import com.rettungshundeEinsatzApp.functions.downloadAllUserData
import com.rettungshundeEinsatzApp.service.myLocation.MyLocationStatus
import com.rettungshundeEinsatzApp.viewmodel.AllTracksViewModel
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Square
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.ScaleBarOverlay
import androidx.core.net.toUri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.edit
import com.rettungshundeEinsatzApp.activity.ManageAreaOverviewActivity
import com.rettungshundeEinsatzApp.activity.ManageTracksOverviewActivity
import com.rettungshundeEinsatzApp.activity.ReportActivity
import com.rettungshundeEinsatzApp.database.area.AreaCoordinateEntity
import com.rettungshundeEinsatzApp.database.area.AreaDatabase
import com.rettungshundeEinsatzApp.database.area.AreaEntity
import com.rettungshundeEinsatzApp.functions.areas.ApiService
import com.rettungshundeEinsatzApp.functions.areas.AreaRepository
import com.rettungshundeEinsatzApp.viewmodel.AreaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.abs

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
    val viewModel: MyTrackViewModel = viewModel(factory = MyTrackViewModel.Factory(myLocationDatabase.locationDao()))
    val myLocations by viewModel.locations.collectAsState()
    val locationToMGRSConverter = MyLocationLatLongToMGRS()
    val db = AllUserDataProvider.getDatabase(context)
    val locationDao = db.allUsersLocationsDao()
    val userDao = db.allUserDataDao()
    val mapCenteredOnce by viewModel.mapCenteredOnce
    val areaCornerMarkers = remember { mutableStateListOf<Marker>() }
    var areaName by rememberSaveable { mutableStateOf("") }
    var areaDescription by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color.Red) }
    var dialogShowSaveArea by remember { mutableStateOf(false) }
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val areaDatabase = AreaDatabase.getDatabase(context)
    val areaDao = areaDatabase.areaDao()
    val retrofit = Retrofit.Builder()
        .baseUrl(serverApiURL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    val areaRepository = remember { AreaRepository(apiService, areaDao) }
    val areaViewModel: AreaViewModel = viewModel(factory = AreaViewModel.Factory(areaDao))
    val allAreas by areaViewModel.areas.collectAsState()
    val lastPointText = stringResource(id = R.string.last_point)
    val radioCallNameText = stringResource(id = R.string.radio_call_name)
    val accuracyText = stringResource(id = R.string.accuracy)
    val uTMMGRSText = stringResource(id = R.string.utm_mgrs)
    val timeText = stringResource(id = R.string.time)
    val unknownText = stringResource(id = R.string.unknown)
    val oSMCopyright = stringResource(id = R.string.osm_copyright)
    val viewModelAllTracks: AllTracksViewModel = viewModel(
        factory = AllTracksViewModel.Factory(locationDao, userDao)
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

    val timeDiffMillis = (1 * 60 * 1000).toLong()
    val distanceMeters = 100.0
    val dummyMapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
        }
    }
    val statusBarPaddingPx = with(LocalDensity.current) {
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding().toPx()
    }

    val scaleBarOverlay = remember(statusBarPaddingPx) {
        ScaleBarOverlay(dummyMapView).apply {
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


    var menuVisible by remember { mutableStateOf(false) }
    BackHandler(enabled = menuVisible) {
        menuVisible = false
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

            MapViewContainer(
                modifier = Modifier.fillMaxSize(),

                // My GPS Data
                myLocations = myLocations,
                myGeoPoints = myLocations.map { GeoPoint(it.latitude, it.longitude) },

                // All User  Data
                allLocations = allLocations,
                allUsers = allUsers,

                // All Area Data
                allAreas = allAreas,

                // my track color
                myTrackColor = myTrackColor,

                // States & Flags
                drawAreaMode = drawAreaMode,
                mapCenteredOnce = mapCenteredOnce,

                // Function
                markAsCentered = { viewModel.markAsCentered() },

                // Area-Editing-States
                areaPoints = areaPoints,
                areaPolygon = areaPolygon,
                areaCornerMarkers = areaCornerMarkers,

                // Security Level
                securityLevel = securityLevel,

                // User information
                myUserName = myUserName,
                radioCallName = radioCallName,

                // Format Strings & Converter
                lastPointText = lastPointText,
                radioCallNameText = radioCallNameText,
                accuracyText = accuracyText,
                uTMMGRSText = uTMMGRSText,
                timeText = timeText,
                unknownText = unknownText,

                locationToMGRSConverter = locationToMGRSConverter,

                // Map scale overlay
                scaleBarOverlay = scaleBarOverlay,

                // Time & distance threshold
                timeDiffMillis = timeDiffMillis,
                distanceMeters = distanceMeters,

            )

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

            if (!drawAreaMode) {

                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 10.dp, bottom = navBarPadding + 30.dp, end = 16.dp)
                        .align(Alignment.BottomEnd)
                ) {

                    if (securityLevel > 1) {

                        // Start create Area Button
                        FloatingActionButton(
                            onClick = {
                                menuVisible = false
                                drawAreaMode = !drawAreaMode
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddBox,
                                contentDescription = stringResource(id = R.string.add_area)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    FloatingActionButton(
                        onClick = {
                            viewModel.resetCentering()
                            if (securityLevel > 1) {

                                CoroutineScope(Dispatchers.IO).launch {
                                    // Do Upload
                                    val (uploadSuccess, uploadMessage) = areaRepository.uploadAreas(token)

                                    if (uploadSuccess) {
                                        areaRepository.downloadAreas(token)
                                    } else {
                                        Log.e("Sync", "Upload failed: $uploadMessage")
                                    }
                                }


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

                            }else{

                                CoroutineScope(Dispatchers.IO).launch {
                                    val (success, message) = areaRepository.downloadAreas(token)
                                    Log.d("MapScreen", "Download Areas: success=$success, message=$message")
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
                        PulsingIcon(
                            icon = Icons.Default.ShareLocation,
                            contentDescription = if (gpsActive || gpsIsRunningPref)
                                stringResource(id = R.string.stop_gps_tracking)
                            else
                                stringResource(id = R.string.start_gps_tracking),
                            pulse = gpsActive || gpsIsRunningPref,
                            activeColor = Color.Red
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
                            areaPoints.clear()
                            areaPolygon.setPoints(emptyList())
                            areaCornerMarkers.clear()
                            drawAreaMode = false
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
                            }
                            if (areaCornerMarkers.isNotEmpty()) {
                                areaCornerMarkers.removeAt(areaCornerMarkers.lastIndex)
                            }
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
                            .align(Alignment.BottomCenter)
                            //.padding(top = 80.dp)
                            .padding(46.dp)
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

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        stringResource(id = R.string.my_position),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    //Spacer(modifier = Modifier.height(5.dp))

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

                    Spacer(modifier = Modifier.height(25.dp))

                    Text(
                        "Menü",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    //Spacer(modifier = Modifier.height(16.dp))

                    // Start/Stop GPS tracking
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
                        PulsingIcon(
                            icon = Icons.Default.ShareLocation,
                            contentDescription = null,
                            pulse = gpsActive // <- hier entscheidet sich das Pulsieren
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (gpsActive) "GPS Stop" else "GPS Start"
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Contacts
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

                    // Manage Areas
                    Button(
                        onClick = {
                            val intent = Intent(context, ManageAreaOverviewActivity::class.java)
                            context.startActivity(intent)
                        },
                        enabled = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Square,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Flächenverwaltung")
                    }

                    if (securityLevel > 1) {

                        Spacer(modifier = Modifier.height(8.dp))

                        // Manage Tracks
                        Button(
                            onClick = {
                                val intent = Intent(context, ManageTracksOverviewActivity::class.java)
                                context.startActivity(intent)
                            },
                            enabled = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Trackverwaltung")
                        }
                    }

                    if (securityLevel > 2) {

                        Spacer(modifier = Modifier.height(8.dp))

                        // Manage Users
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

                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Write Operation Report
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

        if (dialogShowSaveArea) {
            AlertDialog(
                onDismissRequest = { dialogShowSaveArea = false },
                confirmButton = {
                    TextButton(onClick = {

                        val colorHex = String.format("#%06X", 0xFFFFFF and selectedColor.toArgb())

                        Log.d("insertArea", "areaPoints size before save: ${areaPoints.size}")

                        CoroutineScope(Dispatchers.IO).launch {
                            Log.d("MapScreen", "Start Coroutine to Save Area")
                            Log.d("MapScreen", "AreaName on save: '$areaName'")

                            val areaId = areaDao.insertArea(
                                AreaEntity(
                                    title = areaName.ifBlank { "n.n." },
                                    desc = areaDescription.ifBlank { "" },
                                    color = colorHex,
                                    uploadedToServer = false
                                )
                            )

                            val coordinates = areaPoints.mapIndexed { index, point ->
                                AreaCoordinateEntity(
                                    latitude = point.latitude,
                                    longitude = point.longitude,
                                    orderIndex = index,
                                    areaId = areaId.toInt()
                                )
                            }

                            areaDao.insertCoordinates(coordinates)
                            areaPoints.clear()
                            areaName = ""
                            areaDescription = ""
                            dialogShowSaveArea = false
                            selectedColor = Color.Red
                            drawAreaMode = false
                            Log.d("MapScreen", "End Coroutine to Save Area")
                        }
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

                        OutlinedTextField(
                            value = areaDescription,
                            onValueChange = { areaDescription = it },
                            label = { Text(stringResource(id = R.string.area_description_label)) },
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
    return "$direction ${"%.5f".format(abs(longitude))}°"
}

fun formatLatitude(latitude: Double): String {
    val direction = if (latitude >= 0) "N" else "S"
    return "$direction ${"%.5f".format(abs(latitude))}°"
}

fun formatAccuracy(latitude: Float): String {
    return "± ${latitude.toInt()} m"
}

fun getColoredVectorMarker(context: Context, drawableRes: Int, color: Int): Drawable {
    return DrawableCache.coloredMarkers.getOrPut(drawableRes to color) {
        Log.d("DrawableCache", "Creating new marker icon for color=$color")
        val drawable = ContextCompat.getDrawable(context, drawableRes)
            ?: throw IllegalArgumentException("loading Drawable error")
        val wrapped = DrawableCompat.wrap(drawable).mutate()
        DrawableCompat.setTint(wrapped, color)
        wrapped
    }
}

@Composable
fun PulsingIcon(
    icon: ImageVector,    contentDescription: String?,
    modifier: Modifier = Modifier,
    pulse: Boolean = false,
    activeColor: Color = Color.Red, // neu: aktive Farbe
    inactiveColor: Color = LocalContentColor.current // Standardfarbe
) {
    val scale by rememberInfiniteTransition(label = "pulseTransition")
        .animateFloat(
            initialValue = 1f,
            targetValue = if (pulse) 1.2f else 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )

    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = if (pulse) activeColor else inactiveColor, // <--- Farbe wechselt hier
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    )
}
