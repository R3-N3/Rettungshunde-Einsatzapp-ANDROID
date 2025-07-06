package com.rettungshundeEinsatzApp.database.area

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey


@Entity(
    tableName = "area_coordinate",
    foreignKeys = [ForeignKey(
        entity = AreaEntity::class,
        parentColumns = ["id"],
        childColumns = ["areaId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class AreaCoordinateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val orderIndex: Int,
    val areaId: Int
)