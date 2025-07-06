package com.rettungshundeEinsatzApp.database.area

import androidx.room.Entity
import androidx.room.PrimaryKey

// AreaEntity.kt
@Entity(tableName = "area")
data class AreaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val desc: String,
    val color: String,
    val uploadedToServer: Boolean
)