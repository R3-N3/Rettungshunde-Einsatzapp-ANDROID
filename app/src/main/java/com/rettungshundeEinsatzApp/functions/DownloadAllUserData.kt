package com.rettungshundeEinsatzApp.functions

import android.util.Log
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataDao
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataEntity
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

fun downloadAllUserData(
    token: String,
    serverApiURL: String,
    userDao: AllUserDataDao,
    onResponse: (String) -> Unit
) {
    val client = OkHttpClient()
    val serverURLGetUserData = serverApiURL + "downloadalluserdata"

    val formBody = FormBody.Builder()
        .add("token", token)
        .build()

    val request = Request.Builder()
        .url(serverURLGetUserData)
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
                val listType = TypeToken.getParameterized(List::class.java, UserDataDto::class.java).type
                val responseType = TypeToken.getParameterized(ApiResponse::class.java, listType).type
                val parsedResponse: ApiResponse<List<UserDataDto>> = gson.fromJson(jsonData, responseType)

                if (parsedResponse.status == "success") {
                    val userDtoList = parsedResponse.data
                    val entityList = userDtoList.map { dto -> dto.toEntity() }

                    withContext(Dispatchers.IO) {
                        userDao.deleteAll()
                        userDao.insertAll(entityList)

                        /*
                        // To Log Database
                        val allEntries = userDao.getAll()
                        allEntries.forEach { user ->
                            Log.d("GetAllUserData", user.toString())
                        }
                    */
                    }

                    onResponse("success, ${entityList.size} users saved in Room Database")
                    Log.d("DownloadAllUserData","Success")

                } else {
                    onResponse("server responded with status: ${parsedResponse.status}, message: ${parsedResponse.message}")
                    Log.w("DownloadAllUserData","Error: ${parsedResponse.message}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onResponse("error, ${e.message}")
            Log.w("DownloadAllUserData","Error: ${e.message}")
        }
    }
}

fun UserDataDto.toEntity(): AllUserDataEntity {
    return AllUserDataEntity(
        id = this.id,
        username = this.username,
        email = this.email ?: "", // fallback to empty String
        phonenumber = this.phonenumber ?: "",
        securitylevel = this.securitylevel ?: 0,
        radiocallname = this.radiocallname ?: "",
        trackColor = this.trackColor,
        uploadToServerStatus = false
    )
}

data class UserDataDto(
    @SerializedName("ID") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phonenumber") val phonenumber: String? = null,
    @SerializedName("securitylevel") val securitylevel: Int? = null,
    @SerializedName("radiocallname") val radiocallname: String? = null,
    @SerializedName("track_color") val trackColor: String? = null
)
