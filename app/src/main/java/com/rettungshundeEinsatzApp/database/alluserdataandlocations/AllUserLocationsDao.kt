package com.rettungshundeEinsatzApp.database.alluserdataandlocations

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AllUserLocationsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: AllUsersLocationsEntity)

    @Query("SELECT * FROM allUsersLocations")
    suspend fun getAll(): List<AllUsersLocationsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<AllUsersLocationsEntity>)

    @Query("DELETE FROM allUsersLocations")
    suspend fun deleteAll()

    @Query("SELECT * FROM allUsersLocations ORDER BY timestamp ASC")
    fun getAllAsFlow(): Flow<List<AllUsersLocationsEntity>>
}