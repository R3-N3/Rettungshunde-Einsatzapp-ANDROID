package com.rettungshundeEinsatzApp.database.mylocallocation

import androidx.room.*

@Dao
interface MyLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: MyLocationEntity)

    @Query("SELECT * FROM myLocations WHERE uploadToServerStatus = 0")
    suspend fun getAllLocationsWithUploadToServerStatusFalse(): List<MyLocationEntity>

    @Query("UPDATE myLocations SET uploadToServerStatus = 1 WHERE id = :id")
    suspend fun setUploadTrueById(id: Int)

    @Query("DELETE FROM myLocations WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM myLocations")
    suspend fun deleteAll()

    @Query("SELECT * FROM myLocations ORDER BY timestamp ASC")
    fun getAllLocationsFlow(): kotlinx.coroutines.flow.Flow<List<MyLocationEntity>>
}