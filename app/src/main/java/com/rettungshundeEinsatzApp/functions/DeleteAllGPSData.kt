package com.rettungshundeEinsatzApp.functions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserLocationsDao
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import android.util.Log

fun deleteAllGPSData(
    serverApiURL: String,
    token: String,
    allUsersLocationsDao: AllUserLocationsDao,
    onResult: (Boolean, String) -> Unit
) {
    val client = OkHttpClient()

    val requestBody = FormBody.Builder()
        .add("token", token)
        .build()

    val request = Request.Builder()
        .url(serverApiURL +"deleteallgpsdata")
        .post(requestBody)
        .build()

    CoroutineScope(Dispatchers.Main).launch {
        try {
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            response.use {
                if (!it.isSuccessful) throw IOException("Unexpected Code: ${it.code}")
                val jsonData = it.body?.string() ?: "{}"
                val jsonObject = JSONObject(jsonData)
                val status = jsonObject.get("status")
                val message = jsonObject.get("message").toString()
                if (status == "success") {
                    withContext(Dispatchers.IO) {
                        allUsersLocationsDao.deleteAll()
                    }
                    onResult(true, "All Areas deleted.")

                    Log.d("DeleteAllGPSData","Success")
                }else{
                    onResult(false, message)
                    Log.w("DeleteAllGPSData","Error $message")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(false, "${e.message}")
            Log.w("DeleteAllGPSData","Error: ${e.message}")
        }
    }

}