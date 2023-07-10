package com.example.td_test_2.database.entity.weightresult

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "WeightResultTable")
data class WeightResult(
    @PrimaryKey(autoGenerate = true)
    var id : Int,
    var type : String,
    var sentence : String,
    var result : String
)
