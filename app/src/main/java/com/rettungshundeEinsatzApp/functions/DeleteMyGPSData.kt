package com.rettungshundeEinsatzApp.functions

import android.content.Context
import android.util.Log
import com.rettungshundeEinsatzApp.R
import com.rettungshundeEinsatzApp.database.mylocallocation.MyLocationDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun deleteMyGPSData(
    context: Context,
    onResult: (Boolean, String) -> Unit
) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            withContext(Dispatchers.IO) {
                val db = MyLocationDatabase.getDatabase(context.applicationContext)
                db.locationDao().deleteAll()
            }
            onResult(true, context.getString(R.string.my_location_data_deleted))

            Log.d("DeleteMyGPSData","Success")
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(false, "${e.message}")
            Log.w("DeleteMyGPSData","Error: ${e.message}")
        }
    }
}
