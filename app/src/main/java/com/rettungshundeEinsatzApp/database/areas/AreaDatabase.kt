package com.rettungshundeEinsatzApp.database.areas

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SavedArea::class], version = 2)
@TypeConverters(UploadStatusConverter::class)
abstract class AreaDatabase : RoomDatabase() {
    abstract fun areaDao(): AreaDao

    companion object {
        @Volatile private var INSTANCE: AreaDatabase? = null

        fun getDatabase(context: Context): AreaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AreaDatabase::class.java,
                    "area_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}