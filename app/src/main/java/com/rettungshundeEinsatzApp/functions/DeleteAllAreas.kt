package com.rettungshundeEinsatzApp.functions

import android.util.Log
import com.rettungshundeEinsatzApp.database.areas.AreaDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.*

fun deleteAllAreas(
    token: String,
    serverApiURL: String,
    areaDao: AreaDao,
    onResult: (Boolean, String) -> Unit
) {
    val client = OkHttpClient()
    val deleteUrl = serverApiURL + "deleteareas"

    val formBody = FormBody.Builder()
        .add("token", token)
        .build()

    val request = Request.Builder()
        .url(deleteUrl)
        .post(formBody)
        .build()

    CoroutineScope(Dispatchers.Main).launch {
        try {
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            response.use {
                if (!it.isSuccessful) throw Exception("HTTP ${it.code}")
                val responseBody = it.body?.string() ?: ""

                if (responseBody.contains("success", ignoreCase = true)) {
                    withContext(Dispatchers.IO) {
                        areaDao.deleteAll()
                    }
                    onResult(true, "success, Server and local database emptied.")
                    Log.d("DeleteAllAreas","Success:")
                } else {
                    onResult(false, "error, Server response: $responseBody")

                    Log.w("DeleteAllAreas","Error: $responseBody")
                }
            }
        } catch (e: Exception) {
            onResult(false, "error, ${e.message}")
            Log.w("DeleteAllAreas","Error: ${e.message}")
        }
    }
}