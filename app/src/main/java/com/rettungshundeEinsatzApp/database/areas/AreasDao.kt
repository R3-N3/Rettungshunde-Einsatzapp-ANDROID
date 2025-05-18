package com.rettungshundeEinsatzApp.database.areas

import androidx.room.*

@Dao
interface AreaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(area: SavedArea)

    @Query("SELECT * FROM savedAreas")
    suspend fun getAll(): List<SavedArea>

    @Query("SELECT * FROM savedAreas WHERE uploadStatus = :status")
    fun getByStatus(status: UploadStatus): kotlinx.coroutines.flow.Flow<List<SavedArea>>

    @Update
    suspend fun update(area: SavedArea)

    @Delete
    suspend fun delete(area: SavedArea)

    @Query("DELETE FROM savedAreas WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM savedAreas WHERE uploadStatus = :status")
    suspend fun getByStatusList(status: UploadStatus): List<SavedArea>

    @Query("UPDATE savedAreas SET uploadStatus = :status WHERE id = :id")
    suspend fun updateUploadStatus(id: Int, status: UploadStatus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(areas: List<SavedArea>)

    @Query("DELETE FROM savedAreas")
    suspend fun deleteAll()


}