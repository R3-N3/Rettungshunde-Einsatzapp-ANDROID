package com.rettungshundeEinsatzApp.functions.areas

import android.content.Context
import android.util.Log
import com.rettungshundeEinsatzApp.R
import com.rettungshundeEinsatzApp.database.area.AreaDao
import com.rettungshundeEinsatzApp.database.area.AreaEntity
import com.rettungshundeEinsatzApp.database.area.AreaCoordinateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AreaRepository(
    private val apiService: ApiService,
    private val areaDao: AreaDao,
    private val context: Context
) {
    suspend fun downloadAreas(token: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (AreaDownloadManager.isDownloadingAreas) {
            Log.d("uploadAreasToServer", "⚠\uFE0F downloadAreas already running")
            return@withContext Pair(false, context.getString(R.string.already_in_progress))
        }

        AreaDownloadManager.isDownloadingAreas = true
        Log.d("uploadAreasToServer", "\uD83D\uDFE2 start downloadAreasFromServer")

        try {
            val response = apiService.downloadAreas(mapOf("token" to token))
            AreaDownloadManager.isDownloadingAreas = false

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success" && body.data != null) {

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

                        val coordinates = area.points.map {
                            AreaCoordinateEntity(
                                latitude = it.lat,
                                longitude = it.lon,
                                orderIndex = it.orderIndex,
                                areaId = areaId.toInt()
                            )
                        }
                        areaDao.insertCoordinates(coordinates)
                    }

                    Log.d("uploadAreasToServer", "✅ area synced")
                    Pair(true, body.message)

                } else {
                    Pair(false, body?.message ?: context.getString(R.string.unknown_error))
                }
            } else {
                Pair(false, context.getString(R.string.server_error) + response.code())
            }

        } catch (e: Exception) {
            AreaDownloadManager.isDownloadingAreas = false
            Log.e("uploadAreasToServer", "❌ Error: ${e.localizedMessage}")
            Pair(false, e.localizedMessage ?: context.getString(R.string.error))
        }
    }

    suspend fun uploadAreas(token: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val areasToUpload = areaDao.getAreasNotUploaded()

            if (areasToUpload.isEmpty()) {
                Log.d("uploadAreasToServer", "\uD83D\uDFE1 no areas to upload")
                return@withContext Pair(true, context.getString(R.string.no_areas_to_upload))
            }

            areasToUpload.forEach { area ->
                Log.d("uploadAreasToServer", "🟠 AreaDB: ${area.area.title}, Coordinates: ${area.coordinates.size}")
            }

            val uploadAreas = areasToUpload.map { areaWithCoordinates ->
                UploadArea(
                    title = areaWithCoordinates.area.title,
                    description = areaWithCoordinates.area.desc,
                    color = areaWithCoordinates.area.color,
                    points = areaWithCoordinates.coordinates.sortedBy { it.orderIndex }.map { coordinate ->
                        UploadAreaPoint(
                            lat = coordinate.latitude,
                            lon = coordinate.longitude
                        )
                    }
                )
            }

            val payload = UploadAreasRequest(
                token = token,
                areas = uploadAreas
            )

            Log.d("uploadAreasToServer", "➡️ Payload tu upload:")
            uploadAreas.forEach { area ->
                Log.d("uploadAreasToServer", "🔹 Area: ${area.title}, Desc: ${area.description}, Color: ${area.color}")
                area.points.forEach { point ->
                    Log.d("uploadAreasToServer", "   🔸 Point: lat=${point.lat}, lon=${point.lon}")
                }
            }

            val response = apiService.uploadAreas(payload)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success") {
                    Log.d("uploadAreasToServer", "✅ Areas uploaded : ${body.message}")

                    areasToUpload.forEach { area ->
                        areaDao.setAreaUploaded(area.area.id)
                    }

                    return@withContext Pair(true, body.message)
                } else {
                    Log.e("uploadAreasToServer", "❌ Failure: ${body?.message ?: "Unknown Error"}")
                    return@withContext Pair(false, body?.message ?: context.getString(R.string.unknown_error))
                }
            } else {
                Log.e("uploadAreasToServer", "❌ HTTP Error: ${response.code()}")
                return@withContext Pair(false, context.getString(R.string.server_error) + response.code())
            }

        } catch (e: Exception) {
            Log.e("uploadAreasToServer", "❌ Exception: ${e.localizedMessage}")
            return@withContext Pair(false, e.localizedMessage ?: context.getString(R.string.error))
        }
    }
}