package com.rettungshundeEinsatzApp.database.alluserdataandlocations

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "userData")
data class AllUserDataEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id: Int,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "phonenumber")
    val phonenumber: String,

    @ColumnInfo(name = "securitylevel")
    val securitylevel: Int,

    @ColumnInfo(name = "radiocallname")
    val radiocallname: String,

    @ColumnInfo(name = "track_color")
    val trackColor: String?,

    val uploadToServerStatus: Boolean = false
)
