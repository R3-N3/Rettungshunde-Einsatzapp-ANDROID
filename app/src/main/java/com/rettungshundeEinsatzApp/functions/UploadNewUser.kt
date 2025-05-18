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

fun uploadNewUser(
    serverApiURL: String,
    token: String,
    username: String,
    email: String,
    password: String,
    phoneNumber: String,
    callSign: String,
    securityLevel: String,
    colorHex: String,
    onResult: (Boolean, String) -> Unit
) {
    val client = OkHttpClient()

    Log.d("REA-CreateNewUser", "Securitylevel = $securityLevel")

    val requestBody = FormBody.Builder()
        .add("token", token)
        .add("username", username)
        .add("email", email)
        .add("password", password)
        .add("phone", phoneNumber)
        .add("callSign", callSign)
        .add("securelevel", securityLevel)
        .add("color", colorHex)
        .build()

    val request = Request.Builder()
        .url(serverApiURL + "uploadnewuser")
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
                    onResult(true, "User created successfully")
                    Log.w("UploadNewUser","Success")

                }else{
                    onResult(false, message)
                    Log.w("UploadNewUser","Error: $message")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(false, "${e.message}")
            Log.w("UploadNewUser","Error: ${e.message}")
        }
    }

}