package com.rettungshundeEinsatzApp.database.alluserdataandlocations

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AllUserDataEntity::class, AllUsersLocationsEntity::class], version = 1, exportSchema = false)
abstract class AllUserDataAndLocationsDatabase : RoomDatabase() {

    abstract fun allUserDataDao(): AllUserDataDao
    abstract fun allUsersLocationsDao(): AllUserLocationsDao

}