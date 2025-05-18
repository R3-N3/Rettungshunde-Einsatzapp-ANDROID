package com.rettungshundeEinsatzApp.functions


import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

suspend fun uploadReport(
    serverApiURL: String,
    token: String,
    myUserName: String,
    selectedDate: String,
    reportText: String
): Pair<Boolean, String> {
    val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val requestBody = FormBody.Builder()
        .add("token", token)
        .add("username", myUserName)
        .add("date", selectedDate)
        .add("text", reportText)
        .build()

    val request = Request.Builder()
        .url(serverApiURL + "uploadreport")
        .post(requestBody)
        .build()

    return try {
        val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }

        response.use {
            val jsonData = it.body?.string() ?: "{}"
            if (!it.isSuccessful) {
                throw IOException("Unexpected Code: ${it.code}, Response: $jsonData")
            }
            val jsonObject = JSONObject(jsonData)
            val status = jsonObject.get("status")
            val message = jsonObject.get("message").toString()

            if (status == "success") {
                Pair(true, "Report uploaded successfully")
            } else {
                Log.w("UploadReport", "Error: $message")
                Pair(false, message)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Log.w("UploadReport", "Error: ${e.message}")
        Pair(false, e.message ?: "Unknown error")
    }
}