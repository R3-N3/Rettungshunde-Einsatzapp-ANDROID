package com.rettungshundeEinsatzApp.database.areas

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter


@Entity(tableName = "savedAreas")
data class SavedArea(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val timestamp: Long,
    val points: String,
    val color: String,
    val uploadStatus: UploadStatus = UploadStatus.NOT_UPLOADED
)

enum class UploadStatus {
    NOT_UPLOADED,
    RECEIVED
}

class UploadStatusConverter {

    @TypeConverter
    fun fromUploadStatus(status: UploadStatus): String = status.name

    @TypeConverter
    fun toUploadStatus(value: String): UploadStatus =
        try {
            UploadStatus.valueOf(value)
        } catch (e: Exception) {
            UploadStatus.NOT_UPLOADED
        }
}