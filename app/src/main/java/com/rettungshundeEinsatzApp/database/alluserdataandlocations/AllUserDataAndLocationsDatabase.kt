package com.rettungshundeEinsatzApp.database.alluserdataandlocations

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AllUserDataEntity::class, AllUsersLocationsEntity::class], version = 1, exportSchema = false)
abstract class AllUserDataAndLocationsDatabase : RoomDatabase() {

    abstract fun allUserDataDao(): AllUserDataDao
    abstract fun allUsersLocationsDao(): AllUserLocationsDao

    companion object {
        @Volatile
        private var INSTANCE: AllUserDataAndLocationsDatabase? = null

        fun getInstance(context: Context): AllUserDataAndLocationsDatabase {
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
}