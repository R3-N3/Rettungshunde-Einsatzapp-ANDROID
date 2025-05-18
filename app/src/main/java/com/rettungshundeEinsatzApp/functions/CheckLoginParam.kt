package com.rettungshundeEinsatzApp.functions

import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import android.util.Log


val httpClient = OkHttpClient()

fun checkLoginParam(
    username: String,
    password: String,
    serverApiURL: String,
    onResponse: (String) -> Unit
) {
        val formBody = FormBody.Builder()
        .add("username", username)
        .add("password", password)
        .build()

    Log.d("CheckLoginParam",serverApiURL + "login")
    val request = Request.Builder()
        .url(serverApiURL + "login")
        .post(formBody)
        .build()

    val coroutineScope = CoroutineScope(Dispatchers.Main)

    coroutineScope.launch {
        try {
            val response = withContext(Dispatchers.IO) {
                httpClient.newCall(request).execute()
            }

            response.use {
                if (!it.isSuccessful) throw IOException("Unexpected Code: ${it.code}")
                val jsonData = it.body?.string() ?: "{}"
                val jsonObject = JSONObject(jsonData)
                val status = jsonObject.get("status")
                val token = jsonObject.get("token")
                val message = when (status) {
                    "success" -> "Login successful!"
                    "false" -> "User Parameter false"
                    "error" -> jsonObject.getString("message")
                    else -> "Login Unknown Result"
                }
                val messageArray = arrayOf(status, message, token.toString())
                onResponse(messageArray.joinToString(","))
                Log.d("CheckLoginParam","$status $message")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.w("CheckLoginParam","Error: ${e.message}")
            val message = "Error: ${e.message}"
            val messageArray = arrayOf("error", message, "")
            onResponse(messageArray.joinToString(","))
        }
    }
}