package com.rettungshundeEinsatzApp.functions

import androidx.core.content.edit
import com.rettungshundeEinsatzApp.database.mylocallocation.MyLocationDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.Context
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataProvider
import com.rettungshundeEinsatzApp.database.areas.AreaDatabase

fun resetUserData(context: Context) {
    val sharedPreferences = context.getSharedPreferences("REAPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit {
        putString("token", "")
        putString("serverURL", "")
        putString("username", "")
        putString("email", "")
        putString("phoneNumber", "")
        putString("securityLevel", "")
        putString("radioCallName", "")
    }

    val coroutineScope = CoroutineScope(Dispatchers.Main)
    coroutineScope.launch {
        val dbLocation = MyLocationDatabase.getDatabase(context)
        dbLocation.locationDao().deleteAll()

        val dbUserData = AllUserDataProvider.getDatabase(context)
        dbUserData.allUserDataDao().deleteAll()

        val areaDatabase = AreaDatabase.getDatabase(context)
        areaDatabase.areaDao().deleteAll()

        val allLocationDatabase = AllUserDataProvider.getDatabase(context)
        allLocationDatabase.allUsersLocationsDao().deleteAll()

    }
}