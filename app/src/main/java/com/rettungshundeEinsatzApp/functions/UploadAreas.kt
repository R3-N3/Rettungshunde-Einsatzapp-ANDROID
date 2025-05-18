package com.rettungshundeEinsatzApp.functions

import android.util.Log
import com.rettungshundeEinsatzApp.database.areas.AreaDao
import com.rettungshundeEinsatzApp.database.areas.UploadStatus
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody


fun uploadAreas(
    token: String,
    serverApiURL: String,
    areaDao: AreaDao,
    onResponse: (String) -> Unit
) {
    val client = OkHttpClient()
    val uploadUrl = serverApiURL + "uploadarea"

    CoroutineScope(Dispatchers.Main).launch {
        try {
            val notUploadedAreas = withContext(Dispatchers.IO) {
                areaDao.getByStatusList(UploadStatus.NOT_UPLOADED)
            }

            if (notUploadedAreas.isEmpty()) {
                onResponse("success, no new areas tu upload")
                return@launch
            }

            val uploadList = notUploadedAreas.map { area ->
                AreaUploadDto(
                    name = area.name,
                    timestamp = area.timestamp,
                    color = area.color,
                    points = area.points.split(";").mapNotNull {
                        val latlon = it.split(",")
                        if (latlon.size == 2) {
                            val lat = latlon[0].toDoubleOrNull()
                            val lon = latlon[1].toDoubleOrNull()
                            if (lat != null && lon != null) AreaPoint(lat, lon) else null
                        } else null
                    }
                )
            }

            val uploadBody = Gson().toJson(AreaUploadPayload(token, uploadList))
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBody = uploadBody.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .build()

            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            response.use {
                if (!it.isSuccessful) throw Exception("HTTP ${it.code}")
                val responseBody = it.body?.string() ?: ""

                if (responseBody.contains("success", ignoreCase = true)) {
                    withContext(Dispatchers.IO) {
                        notUploadedAreas.forEach { area ->
                            areaDao.deleteById(area.id)
                        }
                    }
                    onResponse("success, ${notUploadedAreas.size} Areas uploaded")
                    Log.d("UploadAreas","Success")
                } else {
                    onResponse("error, Server-Response: $responseBody")
                    Log.w("UploadAreas","Error: $responseBody")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onResponse("error, ${e.message}")
            Log.w("UploadAreas","Error: ${e.message}")
        }
    }
}

data class AreaUploadPayload(
    @SerializedName("token") val token: String,
    @SerializedName("areas") val areas: List<AreaUploadDto>
)

data class AreaUploadDto(
    @SerializedName("name") val name: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("color") val color: String,
    @SerializedName("points") val points: List<AreaPoint>
)

data class AreaPoint(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double
)