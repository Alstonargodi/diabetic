package com.example.td_test_2.database.entity.task

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TodoTable")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val taskID : Int = 0,
    val title: String = "",
    val dateDueMillis : Long = 0L,
    val dateStart: String = "",
    val dateYear : Int = 0,
    val dateMonth : Int = 0,
    val dateDay : Int = 0,
    val startTime : String = "",
    val endTime : String = "",
    val isComplete: Boolean = false,
    var isUploaded : Boolean = false
)