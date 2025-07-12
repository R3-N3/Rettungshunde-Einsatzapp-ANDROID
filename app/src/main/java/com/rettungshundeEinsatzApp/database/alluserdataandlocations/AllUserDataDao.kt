package com.rettungshundeEinsatzApp.database.alluserdataandlocations

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AllUserDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: kotlin.collections.List<com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataEntity>)

    @Query("SELECT * FROM userData")
    suspend fun getAll(): List<AllUserDataEntity>

    @Query("DELETE FROM userData")
    suspend fun deleteAll()

    @Query("SELECT * FROM userData ORDER BY username COLLATE NOCASE ASC")
    fun getAllAsFlow(): Flow<List<AllUserDataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: AllUserDataEntity)

    @Query("SELECT * FROM userData WHERE ID = :id")
    suspend fun getById(id: Int): AllUserDataEntity?

    @Query("""
    SELECT DISTINCT userData.*
    FROM userData
    INNER JOIN allUsersLocations
    ON userData.ID = allUsersLocations.user_id
    ORDER BY username COLLATE NOCASE ASC
""")
    fun getAllUsersWithLocationsAsFlow(): Flow<List<AllUserDataEntity>>
}