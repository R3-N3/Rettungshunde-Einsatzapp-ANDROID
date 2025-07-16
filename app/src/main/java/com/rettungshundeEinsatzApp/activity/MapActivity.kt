package com.rettungshundeEinsatzApp.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.rettungshundeEinsatzApp.R
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataProvider
import com.rettungshundeEinsatzApp.functions.checkTokenAndDownloadMyUserData
import com.rettungshundeEinsatzApp.functions.downloadAllGpsLocations
import com.rettungshundeEinsatzApp.functions.downloadAllUserData
import com.rettungshundeEinsatzApp.functions.resetUserData
import com.rettungshundeEinsatzApp.ui.screens.mapscreen.MapScreen
import com.rettungshundeEinsatzApp.service.myLocation.MyLocationService
import com.rettungshundeEinsatzApp.service.myLocation.MyLocationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MapActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = getSharedPreferences("REAPrefs", MODE_PRIVATE)
        val token = sharedPrefs.getString("token", "") ?: ""
        val serverApiURL = sharedPrefs.getString("serverApiURL", "") ?: ""
        sharedPrefs.edit { putBoolean("gpsRunning", false) }
        MyLocationStatus.setGpsActive(false)

        if (token.isNotEmpty() && serverApiURL.isNotEmpty()) {
            checkTokenAndDownloadMyUserData(token, serverApiURL) { message ->
                val parts = message.split(",")
                when (parts[0]) {
                    "success" -> {
                        sharedPrefs.edit {
                            putString("username", parts[2])
                            putString("email", parts[3])
                            putString("phoneNumber", parts[4])
                            putString("securityLevel", parts[5])
                            putString("radioCallName", parts[6])
                        }
                        Toast.makeText(this, getString(R.string.dashboard_activity_toast_get_user_data_success), Toast.LENGTH_SHORT).show()
                    }

                    "false" -> {
                        Toast.makeText(this, getString(R.string.dashboard_activity_toast_need_new_login), Toast.LENGTH_SHORT).show()
                        resetUserData(this)
                        stopLocationService()
                        startActivity(Intent(this, StartActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    }

                    "error" -> Toast.makeText(this, getString(R.string.toast_connection_error), Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(this, getString(R.string.toast_unknown_error), Toast.LENGTH_SHORT).show()
                }
            }

            Log.d("MapActivity", "Start getAllUserData")
            val db = AllUserDataProvider.getDatabase(this.applicationContext)
            downloadAllUserData(token, serverApiURL, db.allUserDataDao()) { msg ->
                val parts = msg.split(",")
                Log.d("MapActivity", "GetAllUserData: status=${parts[0]}, message=${parts[1]}")
                CoroutineScope(Dispatchers.IO).launch {

                }

                Log.d("MapActivity", "Start getAllGpsLocations")
                val securityLevel = sharedPrefs.getString("securityLevel", "1")?.toIntOrNull() ?: 1
                Log.d("MapActivity", "Security Level: $securityLevel ")
                if (securityLevel > 1) {
                    downloadAllGpsLocations(token, serverApiURL, db.allUsersLocationsDao()) { msg2 ->
                        val parts2 = msg2.split(",", limit = 2)
                        Log.d(
                            "MapActivity",
                            "GetAllGpsLocations: status=${parts2[0]}, message=${parts2.getOrNull(1) ?: ""}"
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            val locations = db.allUsersLocationsDao().getAll()
                            Log.d("MapActivity", "RoomLocation Number of Points: ${locations.size}")
                            /*
                            locations.forEach {
                                Log.d("MapActivity", "RoomLocation: $it")
                            }
                            */
                        }
                    }
                }
            }

        } else {
            Toast.makeText(this, getString(R.string.dashboard_activity_toast_need_new_login), Toast.LENGTH_SHORT).show()
            resetUserData(this)
            stopLocationService()
            startActivity(Intent(this, StartActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }

        setContent {
            MapScreen(
                onStartGPS = { checkPermissionsAndStartService() },
                onStopGPS = { stopLocationService() }
            )
        }
    }

    private fun checkPermissionsAndStartService() {
        if (
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            showPermissionExplanationDialog()
        } else {
            startLocationService()
        }
    }

    private fun startLocationService() {
        val intent = Intent(this, MyLocationService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopLocationService() {
        val intent = Intent(this, MyLocationService::class.java)
        stopService(intent)
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Erst FINE/COARSE und ggf. Notifications
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissionsToRequest.isNotEmpty()) {
            locationPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                startLocationService()
            }
        }
    }

    private val backgroundLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startLocationService()
        } else {
            Toast.makeText(this, getString(R.string.no_background_location_permission), Toast.LENGTH_LONG).show()
        }
    }

    private val locationPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            startLocationService()
        } else {
            Log.d("MapActivity", "Permission not granted")

            // Feedback an den User
            val deniedPermissions = permissions.filterValues { !it }.keys
            val deniedList = deniedPermissions.joinToString(", ") { perm ->
                when (perm) {
                    Manifest.permission.ACCESS_FINE_LOCATION -> getString(R.string.location_fine)
                    Manifest.permission.ACCESS_COARSE_LOCATION -> getString(R.string.location_coarse)
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION -> getString(R.string.location_background)
                    Manifest.permission.POST_NOTIFICATIONS -> getString(R.string.notification)
                    else -> getString(R.string.unknown)
                }
            }

            Toast.makeText(
                this,
                "${getString(R.string.no_granted_location)}: $deniedList",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.location_permission_info_title))
            .setMessage(getString(R.string.location_permission_info_text))
            .setPositiveButton(getString(R.string.next)) { _, _ -> requestPermissions() }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            .show()
    }
}


