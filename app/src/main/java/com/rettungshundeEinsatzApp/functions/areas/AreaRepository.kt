package com.rettungshundeEinsatzApp.functions.areas

import android.util.Log
import com.rettungshundeEinsatzApp.database.area.AreaDao
import com.rettungshundeEinsatzApp.database.area.AreaEntity
import com.rettungshundeEinsatzApp.database.area.AreaCoordinateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AreaRepository(
    private val apiService: ApiService,
    private val areaDao: AreaDao
) {
    suspend fun downloadAreas(token: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (AreaDownloadManager.isDownloadingAreas) {
            Log.d("uploadAreasToServer", "⚠\uFE0F downloadAreas already running")
            return@withContext Pair(false, "Bereits in Bearbeitung")
        }

        AreaDownloadManager.isDownloadingAreas = true
        Log.d("uploadAreasToServer", "\uD83D\uDFE2 Starte downloadAreasFromServer")

        try {
            val response = apiService.downloadAreas(mapOf("token" to token))
            AreaDownloadManager.isDownloadingAreas = false

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success" && body.data != null) {

                    // ➡️ Room: Löschen + Insert in einer Transaction
                    areaDao.deleteAllAreas()

                    body.data.forEach { area ->
                        val areaId = areaDao.insertArea(
                            AreaEntity(
                                title = area.title,
                                desc = area.description,
                                color = area.color,
                                uploadedToServer = true
                            )
                        )

                        val coords = area.points.map {
                            AreaCoordinateEntity(
                                latitude = it.lat,
                                longitude = it.lon,
                                orderIndex = it.order_index,
                                areaId = areaId.toInt()
                            )
                        }
                        areaDao.insertCoordinates(coords)
                    }

                    Log.d("uploadAreasToServer", "✅ Flächen erfolgreich synchronisiert")
                    Pair(true, body.message)

                } else {
                    Pair(false, body?.message ?: "Unbekannter Fehler")
                }
            } else {
                Pair(false, "Server Error: ${response.code()}")
            }

        } catch (e: Exception) {
            AreaDownloadManager.isDownloadingAreas = false
            Log.e("uploadAreasToServer", "❌ Error: ${e.localizedMessage}")
            Pair(false, e.localizedMessage ?: "Fehler")
        }
    }

    suspend fun uploadAreas(token: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            // ➡️ Alle Areas mit uploadedToServer == false laden
            val areasToUpload = areaDao.getAreasNotUploaded()

            if (areasToUpload.isEmpty()) {
                Log.d("uploadAreasToServer", "\uD83D\uDFE1 Keine Flächen zum Hochladen")
                return@withContext Pair(true, "Keine Flächen zum Hochladen")
            }

            areasToUpload.forEach { area ->
                Log.d("uploadAreasToServer", "🟠 AreaDB: ${area.area.title}, Coords: ${area.coordinates.size}")
            }

            // ➡️ Mapping
            val uploadAreas = areasToUpload.map { areaWithCoords ->
                UploadArea(
                    title = areaWithCoords.area.title,
                    description = areaWithCoords.area.desc,
                    color = areaWithCoords.area.color,
                    points = areaWithCoords.coordinates.sortedBy { it.orderIndex }.map { coord ->
                        UploadAreaPoint(
                            lat = coord.latitude,
                            lon = coord.longitude
                        )
                    }
                )
            }

            val payload = UploadAreasRequest(
                token = token,
                areas = uploadAreas
            )

            Log.d("uploadAreasToServer", "➡️ Payload zum Hochladen:")
            uploadAreas.forEach { area ->
                Log.d("uploadAreasToServer", "🔹 Area: ${area.title}, Desc: ${area.description}, Color: ${area.color}")
                area.points.forEach { point ->
                    Log.d("uploadAreasToServer", "   🔸 Point: lat=${point.lat}, lon=${point.lon}")
                }
            }


            // ➡️ HTTP POST ausführen
            val response = apiService.uploadAreas(payload)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success") {
                    Log.d("uploadAreasToServer", "✅ Flächen erfolgreich hochgeladen: ${body.message}")

                    // ➡️ Nach Erfolg: uploadedToServer = true setzen
                    areasToUpload.forEach { area ->
                        areaDao.setAreaUploaded(area.area.id)
                    }

                    return@withContext Pair(true, body.message)
                } else {
                    Log.e("uploadAreasToServer", "❌ Failure: ${body?.message ?: "Unbekannter Fehler"}")
                    return@withContext Pair(false, body?.message ?: "Unbekannter Fehler")
                }
            } else {
                Log.e("uploadAreasToServer", "❌ HTTP Error: ${response.code()}")
                return@withContext Pair(false, "Serverfehler: ${response.code()}")
            }

        } catch (e: Exception) {
            Log.e("uploadAreasToServer", "❌ Exception: ${e.localizedMessage}")
            return@withContext Pair(false, e.localizedMessage ?: "Fehler")
        }
    }
}