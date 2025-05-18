package com.rettungshundeEinsatzApp.functions

import com.rettungshundeEinsatzApp.database.areas.AreaDao
import com.rettungshundeEinsatzApp.database.areas.SavedArea
import com.rettungshundeEinsatzApp.database.areas.UploadStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.*
import java.io.IOException
import android.util.Log

fun downloadAreas(
    token: String,
    serverApiUrl: String,
    areaDao: AreaDao,
    onResponse: (String) -> Unit
) {
    val client = OkHttpClient()
    val url = serverApiUrl + "downloadareas"

    val formBody = FormBody.Builder()
        .add("token", token)
        .build()

    val request = Request.Builder()
        .url(url)
        .post(formBody)
        .build()

    CoroutineScope(Dispatchers.Main).launch {
        try {
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            response.use { it ->
                if (!it.isSuccessful) throw IOException("Server Error: ${it.code}")

                val body = it.body?.string() ?: ""
                val gson = Gson()
                val listType = TypeToken.getParameterized(List::class.java, AreaDto::class.java).type
                val responseType = TypeToken.getParameterized(ApiResponse::class.java, listType).type
                val parsed: ApiResponse<List<AreaDto>> = gson.fromJson(body, responseType)

                if (parsed.status != "success") {
                    onResponse("error, ${parsed.message}")
                    return@launch
                }

                val areas = parsed.data.map { dto ->
                    val pointString = dto.points.joinToString(";") {
                        "${it.lat},${it.lon}"
                    }

                    SavedArea(
                        name = dto.name,
                        timestamp = dto.timestamp,
                        points = pointString,
                        color = dto.color,
                        uploadStatus = UploadStatus.RECEIVED
                    )
                }

                withContext(Dispatchers.IO) {
                    areaDao.deleteAll()
                    areaDao.insertAll(areas)

/*
                    // To Log Database
                    val allEntries = areaDao.getAll()
                    allEntries.forEach { user ->
                        Log.d("Get Areas", user.toString())
                    }
*/
                }
                onResponse("success, ${areas.size} Areas saved")
                Log.d("DownloadAreas","Success")
            }
        } catch (e: Exception) {
            onResponse("error, ${e.message}")
            Log.w("DownloadAreas","Error: ${e.message}")
        }
    }
}

// DTOs
data class AreaDto(
    val name: String,
    val color: String,
    val timestamp: Long,
    val points: List<PointDto>
)

data class PointDto(
    val lat: Double,
    val lon: Double
)