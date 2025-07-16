package com.rettungshundeEinsatzApp.functions

import android.content.Context
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import android.util.Log
import com.rettungshundeEinsatzApp.R


val httpClient = OkHttpClient()

fun checkLoginParam(
    context: Context,
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
                    "success" -> context.getString(R.string.login_successful)
                    "false" -> context.getString(R.string.user_parameter_false)
                    "error" -> jsonObject.getString("message")
                    else -> context.getString(R.string.login_unknown_result)
                }
                val messageArray = arrayOf(status, message, token.toString())
                onResponse(messageArray.joinToString(","))
                Log.d("CheckLoginParam","$status $message")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.w("CheckLoginParam","Error: ${e.message}")
            val message = context.getString(R.string.error) +  e.message
            val messageArray = arrayOf("error", message, "")
            onResponse(messageArray.joinToString(","))
        }
    }
}