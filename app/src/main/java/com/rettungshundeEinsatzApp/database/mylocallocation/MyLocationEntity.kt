package com.rettungshundeEinsatzApp.database.mylocallocation

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "myLocations")
data class MyLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val uploadToServerStatus: Boolean = false
)


