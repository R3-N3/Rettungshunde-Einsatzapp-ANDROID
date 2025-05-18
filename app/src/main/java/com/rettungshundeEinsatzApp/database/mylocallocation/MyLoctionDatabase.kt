package com.rettungshundeEinsatzApp.database.mylocallocation

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MyLocationEntity::class], version = 3, exportSchema = false)
abstract class MyLocationDatabase : RoomDatabase() {

    abstract fun locationDao(): MyLocationDao

    companion object {
        @Volatile
        private var INSTANCE: MyLocationDatabase? = null

        fun getDatabase(context: Context): MyLocationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyLocationDatabase::class.java,
                    "rea_location_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}