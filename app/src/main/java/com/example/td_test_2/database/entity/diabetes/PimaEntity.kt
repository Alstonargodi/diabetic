package com.example.ad_rf.dbconfig.diabetes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dataTable")
data class PimaEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Int,
    val pregnan : String,
    val glucose : String,
    val bloodPressure : String,
    val skinThich : String,
    val insulin : String,
    val bmi : String,
    val pedigree : String,
    val age : String,
    val outcome : String
)