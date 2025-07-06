package com.rettungshundeEinsatzApp.database.area

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AreaEntity::class, AreaCoordinateEntity::class], version = 1)
abstract class AreaDatabase : RoomDatabase() {
    abstract fun areaDao(): AreaDao

    companion object {
        @Volatile
        private var INSTANCE: AreaDatabase? = null

        fun getDatabase(context: Context): AreaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AreaDatabase::class.java,
                    "area_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}