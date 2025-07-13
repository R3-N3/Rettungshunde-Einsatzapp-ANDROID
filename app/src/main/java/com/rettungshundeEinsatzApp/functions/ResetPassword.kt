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
import java.io.IOException

fun resetPassword(
    serverApiURL: String,
    email: String,
    onResult: (Boolean, String) -> Unit
) {
    val client = OkHttpClient()


    val requestBody = FormBody.Builder()
        .add("email", email)
        .build()

    val request = Request.Builder()
        .url(serverApiURL + "resetpassword")
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

                Log.d("resetPassword", "HTTP response body: $jsonData")

                if (jsonData.isBlank()) {
                    onResult(false, "Leere Antwort vom Server.")
                    return@use
                }

                val jsonObject = JSONObject(jsonData)
                val status = jsonObject.get("status")
                val message = jsonObject.get("message").toString()

                if(status == "success"){
                    onResult(true, message)
                    Log.d("resetPassword","Data submitted. if E-Mail ost correct, password will be reset")

                }else{
                    onResult(false, message)
                    Log.w("resetPassword","Error: $message")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(false, "${e.message}")
            Log.w("resetPassword","Error: ${e.message}")
        }
    }

}