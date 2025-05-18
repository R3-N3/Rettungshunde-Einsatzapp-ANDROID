package com.rettungshundeEinsatzApp.functions

import android.util.Log
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserLocationsDao
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUsersLocationsEntity
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request
import java.io.IOException
import okhttp3.*

fun downloadAllGpsLocations(
    token: String,
    serverApiURL: String,
    locationDao: AllUserLocationsDao,
    onResponse: (String) -> Unit
) {
    val client = OkHttpClient()
    val serverURLGetLocations = serverApiURL + "downloadalluserlocation"

    val formBody = FormBody.Builder()
        .add("token", token)
        .build()

    val request = Request.Builder()
        .url(serverURLGetLocations)
        .post(formBody)
        .build()

    CoroutineScope(Dispatchers.Main).launch {
        try {
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            response.use {
                if (!it.isSuccessful) throw IOException("Unexpected code: ${it.code}")
                val jsonData = it.body?.string() ?: "[]"

                val gson = Gson()
                val listType = TypeToken.getParameterized(List::class.java, LocationDto::class.java).type
                val responseType = TypeToken.getParameterized(ApiResponse::class.java, listType).type
                val parsedResponse: ApiResponse<List<LocationDto>> = gson.fromJson(jsonData, responseType)

                if (parsedResponse.status == "success") {
                    val locationEntities = parsedResponse.data.map { dto -> dto.toEntity() }

                    withContext(Dispatchers.IO) {
                        locationDao.deleteAll()
                        locationDao.insertAll(locationEntities)

                        /*
                        // To Log Database
                        val allEntries = locationDao.getAll()
                        allEntries.forEach { user ->
                            Log.d("GetAllLocationData", user.toString())
                        }
                        */
                    }

                    onResponse("success, ${locationEntities.size} GPS points saved")
                    Log.d("DownloadAllLocationsData","Success")

                } else {
                    onResponse("server responded with status: ${parsedResponse.status}, message: ${parsedResponse.message}")
                    Log.w("DownloadAllLocationsData","Error: ${parsedResponse.message}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onResponse("error, ${e.message}")
            Log.w("DownloadAllLocationsData","Error: ${e.message}")
        }
    }
}

fun LocationDto.toEntity(): AllUsersLocationsEntity {
    return AllUsersLocationsEntity(
        id = this.id,
        userId = this.userId,
        latitude = this.latitude,
        longitude = this.longitude,
        timestamp = this.timestamp,
        accuracy = this.accuracy
    )
}

data class LocationDto(
    @SerializedName("id") val id: Long,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("accuracy") val accuracy: Int
)

