package com.rettungshundeEinsatzApp.database.area

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import androidx.room.Transaction


@Dao
interface AreaDao {
    @Query("DELETE FROM area")
    suspend fun deleteAllAreas()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArea(area: AreaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoordinates(coords: List<AreaCoordinateEntity>)

    @Transaction
    @Query("SELECT * FROM area")
    fun getAllAreasWithCoordinatesFlow(): kotlinx.coroutines.flow.Flow<List<AreaWithCoordinates>>

    @Transaction
    @Query("SELECT * FROM area WHERE uploadedToServer = 0")
    suspend fun getAreasNotUploaded(): List<AreaWithCoordinates>

    @Query("UPDATE area SET uploadedToServer = 1 WHERE id = :areaId")
    suspend fun setAreaUploaded(areaId: Int)
}