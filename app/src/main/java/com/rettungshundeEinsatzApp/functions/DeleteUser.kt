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

fun deleteUser(
    serverApiURL: String,
    token: String,
    username: String,
    onResult: (Boolean, String) -> Unit
) {
    val client = OkHttpClient()

    Log.d("Test","Test $username")

    val requestBody = FormBody.Builder()
        .add("token", token)
        .add("username", username)
        .build()

    val request = Request.Builder()
        .url(serverApiURL + "deleteuser")
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

                if(status == "success"){
                    onResult(true, message)
                    Log.d("DeleteUser","Success")

                }else{
                    onResult(false, message)
                    Log.w("DeleteUser","Error: $message")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(false, "${e.message}")
            Log.w("DeleteUser","Error: ${e.message}")
        }
    }

}