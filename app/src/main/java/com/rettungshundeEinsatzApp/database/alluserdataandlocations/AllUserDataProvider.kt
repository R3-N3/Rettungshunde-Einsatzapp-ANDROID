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
                "rea_all_user_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}