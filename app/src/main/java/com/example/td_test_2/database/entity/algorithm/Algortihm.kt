package com.example.td_test_2.database.entity.algorithm

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "algorithmTable")
data class Algortihm(
    @PrimaryKey(autoGenerate = false)
    var id : Int,
    var type : Int,
)
