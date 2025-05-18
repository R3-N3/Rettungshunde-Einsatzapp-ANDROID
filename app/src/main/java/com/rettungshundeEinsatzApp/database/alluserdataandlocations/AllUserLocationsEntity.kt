package com.rettungshundeEinsatzApp.database.alluserdataandlocations

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "allUsersLocations",
    foreignKeys = [
        ForeignKey(
            entity = AllUserDataEntity::class,
            parentColumns = ["ID"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["user_id"])]
)
data class AllUsersLocationsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "timestamp")
    val timestamp: String,

    @ColumnInfo(name = "accuracy")
    val accuracy: Int
)