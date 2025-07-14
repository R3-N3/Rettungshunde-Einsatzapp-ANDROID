package com.rettungshundeEinsatzApp.functions

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataAndLocationsDatabase
import com.rettungshundeEinsatzApp.database.mylocallocation.MyLocationDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

suspend fun exportAndShareGPX(context: Context) {
    val userDb = AllUserDataAndLocationsDatabase.getInstance(context)
    val myDb = MyLocationDatabase.getDatabase(context)

    val myLocations = withContext(Dispatchers.IO) {
        myDb.locationDao().getAllLocationsWithUploadToServerStatusFalse()
    }

    val allUsers = withContext(Dispatchers.IO) {
        userDb.allUserDataDao().getAll()
    }

    val allUserLocations = withContext(Dispatchers.IO) {
        userDb.allUsersLocationsDao().getAll()
    }

    // GPX-Builder
    val gpxBuilder = StringBuilder()
    gpxBuilder.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
    gpxBuilder.appendLine("""<gpx version="1.1" creator="RettungshundeEinsatzApp" xmlns="http://www.topografix.com/GPX/1/1">""")

    val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    isoFormatter.timeZone = TimeZone.getTimeZone("UTC")

    // Eigene Trackpunkte
    if (myLocations.isNotEmpty()) {
        gpxBuilder.appendLine("<trk><name>Mein Track</name><trkseg>")
        for (loc in myLocations) {
            val timestamp = isoFormatter.format(Date(loc.timestamp))
            gpxBuilder.appendLine("""<trkpt lat="${loc.latitude}" lon="${loc.longitude}"><time>$timestamp</time></trkpt>""")
        }
        gpxBuilder.appendLine("</trkseg></trk>")
    }

    // Alle Benutzer
    for (user in allUsers) {
        val userLocations = allUserLocations.filter { it.userId == user.id }.sortedBy { it.timestamp }
        if (userLocations.isNotEmpty()) {
            gpxBuilder.appendLine("<trk><name>${user.username}</name><trkseg>")
            for (loc in userLocations) {
                val timestamp = loc.timestamp
                val parsedTime = try {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(timestamp)
                } catch (e: Exception) {
                    null
                }
                val time = isoFormatter.format(parsedTime ?: Date())
                gpxBuilder.appendLine("""<trkpt lat="${loc.latitude}" lon="${loc.longitude}"><time>$time</time></trkpt>""")
            }
            gpxBuilder.appendLine("</trkseg></trk>")
        }
    }

    gpxBuilder.appendLine("</gpx>")

    // Datei speichern
    val gpxData = gpxBuilder.toString()
    val fileName = "tracks_export_${System.currentTimeMillis()}.gpx"
    val file = File(context.cacheDir, fileName)
    file.writeText(gpxData)

    // Datei-URI mit FileProvider
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider", // z. B. com.deinapp.fileprovider
        file
    )

    // Share Intent
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/gpx+xml"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, "GPX-Datei teilen"))
}