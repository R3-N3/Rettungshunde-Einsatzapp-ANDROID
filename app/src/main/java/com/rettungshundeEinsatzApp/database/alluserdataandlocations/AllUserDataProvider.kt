package com.rettungshundeEinsatzApp.database.alluserdataandlocations


import android.content.Context
import androidx.room.Room

object AllUserDataProvider {
    @Volatile private var INSTANCE: AllUserDataAndLocationsDatabase? = null

    fun getDatabase(context: Context): AllUserDataAndLocationsDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AllUserDataAndLocationsDatabase::class.java,
                "all_user_data_and_locations_db"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}