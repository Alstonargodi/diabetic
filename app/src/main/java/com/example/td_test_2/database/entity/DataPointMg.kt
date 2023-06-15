package com.example.td_test_2.database.entity

import com.example.td_test_2.classification.data.splitWords

data class DataPointMg(
    val point : String,
    val values : MutableMap<String,Any?>
){
    val feature = point.splitWords().distinct().toList()
}