package com.rettungshundeEinsatzApp.service.myLocation

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.rettungshundeEinsatzApp.database.mylocallocation.MyLocationDatabase
import com.rettungshundeEinsatzApp.database.mylocallocation.MyLocationEntity
import com.rettungshundeEinsatzApp.R
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.core.content.edit

class MyLocationService : Service() {

    companion object {
        const val ACTION_STOP_LOCATION = "com.rettungshundeEinsatzApp.ACTION_STOP_LOCATION"
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest


    // CoroutineScope for hole service
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // OkHttpClient mit Timeout
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val sharedPrefs = getSharedPreferences("REAPrefs", MODE_PRIVATE)
            sharedPrefs.edit { putBoolean("gpsRunning", true) }
            for (location in locationResult.locations) {

                val converter = MyLocationLatLongToMGRS()
                val locationInMGRS = converter.convert(location.latitude, location.longitude)
                val date = Date(location.time)
                val format = SimpleDateFormat("dd.MM.yyyy      HH:mm", Locale.getDefault())

                Log.d("LocationService", "$locationInMGRS    Lat: ${location.latitude}°, Lng: ${location.longitude}°, Accuracy: ${location.accuracy}m, Time: ${format.format(date)}")
                MyLocationData.updateLocation(location)
                MyLocationData.updateLocationMGRS(locationInMGRS)
                MyLocationData.updateLocationTime(format.format(date))
                sendLocationToServer(location)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000) // 5 Sek
            .setMinUpdateIntervalMillis(2500)
            //.setMaxUpdateDelayMillis(45000)
            .setGranularity(Granularity.GRANULARITY_FINE)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == ACTION_STOP_LOCATION) {
            stopSelf()
            return START_NOT_STICKY
        }

        val sharedPrefs = getSharedPreferences("REAPrefs", MODE_PRIVATE)
        sharedPrefs.edit { putBoolean("gpsRunning", true) }

        MyLocationStatus.setGpsActive(true)

        startForegroundService()
        startLocationUpdates()
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "LocationServiceChannel"
        val channel = NotificationChannel(
            channelId,
            "GPS Tracking",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 200, 300)
            setSound(
                android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            )
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        // ACTION-BUTTON für "Stoppen"
        val stopIntent = Intent(this, StopLocationServiceReceiver::class.java)
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.gps_tacking_running_title))
            .setContentText(getString(R.string.gps_tacking_running_text))
            .setSmallIcon(R.drawable.notification_icon_rea2)
            .addAction(android.R.drawable.ic_delete, getString(R.string.stop_gps_tracking), stopPendingIntent) // <- Action Button
            .build()

        startForeground(1, notification)
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private fun sendLocationToServer(location: Location) {
        serviceScope.launch {
            val sharedPreferences = getSharedPreferences("REAPrefs", MODE_PRIVATE)
            val token: String = sharedPreferences.getString("token", "").toString()
            val serverApiURL = sharedPreferences.getString("serverApiURL", "").toString() + "uploadmygpspoint"
            val formattedTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date(location.time))

            val formBody = FormBody.Builder()
                .add("latitude", location.latitude.toString())
                .add("longitude", location.longitude.toString())
                .add("accuracy", location.accuracy.toInt().toString())
                .add("token", token)
                .add("timestamp", formattedTimestamp)
                .build()

            val request = Request.Builder()
                .url(serverApiURL)
                .post(formBody)
                .build()

            try {
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }
                response.use {
                    if (!it.isSuccessful) throw java.io.IOException("HTTP Error Code: ${it.code}")
                    val jsonData = it.body?.string() ?: "{}"
                    val jsonObject = JSONObject(jsonData)
                    val status = jsonObject.getString("status")
                    val message = jsonObject.getString("message")

                    if (status == "success") {
                        Log.d("LocationService", "GPS Data successful uploaded: $message")
                        saveLocationLocal(location.latitude, location.longitude, location.accuracy, true)
                        checkAndUploadOfflineLocations()
                    } else {
                        Log.d("LocationService", "GPS Data not accepted: $message")
                        saveLocationLocal(location.latitude, location.longitude, location.accuracy, false)
                    }
                }
            } catch (e: Exception) {
                Log.e("LocationService", "Error while uploading GPS data: ${e.message}")
                saveLocationLocal(location.latitude, location.longitude, location.accuracy, false)
            }
        }
    }

    private suspend fun saveLocationLocal(latitude: Double, longitude: Double, accuracy: Float, uploadToServerStatus: Boolean) {
        val db = MyLocationDatabase.getDatabase(applicationContext)
        db.locationDao().insert(MyLocationEntity(latitude = latitude, longitude = longitude, accuracy = accuracy, uploadToServerStatus = uploadToServerStatus))
    }

    private fun checkAndUploadOfflineLocations() {
        serviceScope.launch {
            val db = MyLocationDatabase.getDatabase(applicationContext)
            val locations = db.locationDao().getAllLocationsWithUploadToServerStatusFalse()

            for (location in locations) {
                val sharedPreferences = getSharedPreferences("REAPrefs", MODE_PRIVATE)
                val token: String = sharedPreferences.getString("token", "").toString()
                val serverApiURL = sharedPreferences.getString("serverApiURL", "").toString() + "uploadmygpspoint"
                val formattedTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(location.timestamp))

                val formBody = FormBody.Builder()
                    .add("latitude", location.latitude.toString())
                    .add("longitude", location.longitude.toString())
                    .add("accuracy", location.accuracy.toString())
                    .add("token", token)
                    .add("timestamp", formattedTimestamp)
                    .build()

                val request = Request.Builder()
                    .url(serverApiURL)
                    .post(formBody)
                    .build()

                try {
                    val response = withContext(Dispatchers.IO) {
                        client.newCall(request).execute()
                    }
                    response.use {
                        if (!it.isSuccessful) throw java.io.IOException("Unexpected Code: ${it.code}")
                        val jsonData = it.body?.string() ?: "{}"
                        val jsonObject = JSONObject(jsonData)
                        val status = jsonObject.getString("status")
                        val message = jsonObject.getString("message")

                        if (status == "success") {
                            Log.d("LocationService", "Offline GPS Data successful uploaded: $message")
                            db.locationDao().setUploadTrueById(location.id)
                        } else {
                            Log.d("LocationService", "Offline GPS Data not accepted: $message")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("LocationService", "Error while uploading offline GPS data: ${e.message}")
                }
            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        val sharedPrefs = getSharedPreferences("REAPrefs", MODE_PRIVATE)
        sharedPrefs.edit { putBoolean("gpsRunning", false) }
        MyLocationStatus.setGpsActive(false)

        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}