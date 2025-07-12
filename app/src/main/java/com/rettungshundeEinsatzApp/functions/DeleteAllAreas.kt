package com.rettungshundeEinsatzApp.functions

import android.util.Log
import com.rettungshundeEinsatzApp.database.area.AreaDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

fun deleteAllAreas(
    serverApiURL: String,
    token: String,
    areaDao: AreaDao,
    onResult: (Boolean, String) -> Unit
) {
    val client = OkHttpClient()

    Log.d("DeleteAllAreas", "Token: '$token'")



    val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    val jsonBody = JSONObject().put("token", token).toString()
    val requestBody = jsonBody.toRequestBody(jsonMediaType)

    val request = Request.Builder()
        .url(serverApiURL + "deleteareas")
        .post(requestBody)
        .build()

    CoroutineScope(Dispatchers.Main).launch {
        try {
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            response.use {
                if (!it.isSuccessful) throw IOException("Unexpected code: ${it.code}")
                val responseData = it.body?.string() ?: "{}"
                val json = JSONObject(responseData)
                val status = json.optString("status")
                val message = json.optString("message")

                if (status == "success") {
                    withContext(Dispatchers.IO) {
                        areaDao.deleteAllAreas()
                    }
                    Log.d("DeleteAllAreas", "✅ Alle Flächen auf Server gelöscht")
                    onResult(true, message)
                } else {
                    Log.w("DeleteAllAreas", "⚠️ Fehler: $message")
                    onResult(false, message)
                }
            }
        } catch (e: Exception) {
            Log.e("DeleteAllAreas", "❌ Fehler: ${e.message}", e)
            onResult(false, e.message ?: "Unbekannter Fehler")
        }
    }
}