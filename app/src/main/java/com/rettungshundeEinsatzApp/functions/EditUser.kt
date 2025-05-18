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

fun editUser(
    serverApiURL: String,
    token: String,
    username: String,
    email: String,
    phoneNumber: String,
    callSign: String,
    selectedSecurityLevelSend: String,
    selectedHex: String,
    userID: String,
    onResult: (Boolean, String) -> Unit
) {
    val client = OkHttpClient()

    val requestBody = FormBody.Builder()
        .add("token", token)
        .add("username", username)
        .add("email", email)
        .add("phoneNumber", phoneNumber)
        .add("callSign", callSign)
        .add("selectedSecurityLevelSend", selectedSecurityLevelSend)
        .add("selectedHex", selectedHex)
        .add("userID", userID)
        .build()



    val request = Request.Builder()
        .url(serverApiURL + "edituser")
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
                    Log.d("EditUser","Success")
                }else{
                    onResult(false, message)
                    Log.w("EditUser","Error: $message")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(false, "${e.message}")
            Log.w("EditUser","Error: ${e.message}")
        }
    }

}