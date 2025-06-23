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

fun editMyUserData(
    serverApiURL: String,
    token: String,
    email: String,
    phoneNumber: String,
    onResult: (Boolean, String) -> Unit
) {
    val client = OkHttpClient()

    val requestBody = FormBody.Builder()
        .add("token", token)
        .add("email", email)
        .add("phoneNumber", phoneNumber)
        .build()



    val request = Request.Builder()
        .url(serverApiURL + "editmyuserdata")
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
                    Log.d("editMyUserData","Success")
                }else{
                    onResult(false, message)
                    Log.w("editMyUserData","Error: $message")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(false, "${e.message}")
            Log.w("editMyUserData","Error: ${e.message}")
        }
    }

}