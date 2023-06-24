package com.example.td_test_2.database.entity.words

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kalimatTable")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    var id : Int,
    var type : String,
    var sentence : String,
    var result : String
)
