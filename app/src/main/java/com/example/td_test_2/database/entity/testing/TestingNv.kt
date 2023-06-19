package com.example.td_test_2.database.entity.testing

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "testingNvTable")
data class TestingNv(
    @PrimaryKey(autoGenerate = false)
    val input : String,
    val label : String,
    val predictResult : String,
    val output : String,
    val computationTime : String,
)
