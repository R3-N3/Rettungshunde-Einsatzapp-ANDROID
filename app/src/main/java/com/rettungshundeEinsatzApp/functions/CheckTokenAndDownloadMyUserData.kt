package com.rettungshundeEinsatzApp.functions

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

fun checkTokenAndDownloadMyUserData(
    token: String,
    serverApiURL: String,
    onResponse: (String) -> Unit
) {
    val client = OkHttpClient()
    val formBody = FormBody.Builder().add("token", token).build()
    val request = Request.Builder().url(serverApiURL + "downloadmyuserdata").post(formBody).build()

    CoroutineScope(Dispatchers.Main).launch {
        try {
            val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
            response.use {
                val json = JSONObject(it.body?.string() ?: "{}")
                val status = json.getString("status")
                val message = json.getString("message")
                if (status == "success") {
                    val array = arrayOf(
                        status,
                        message,
                        json.getString("username"),
                        json.getString("email"),
                        json.getString("phoneNumber"),
                        json.getString("securityLevel"),
                        json.getString("radioCallName")
                    )
                    onResponse(array.joinToString(","))
                    Log.d("CheckTokenAndDownloadMyUserData","Success")
                } else {
                    onResponse(arrayOf(status, message).joinToString(","))
                    Log.w("CheckTokenAndDownloadMyUserData","Status: $status message: $message")
                }
            }
        } catch (e: Exception) {
            Log.w("CheckTokenAndDownloadMyUserData","Error: ${e.message}")
            onResponse("error,Error 003: ${e.message}")
        }
    }
}