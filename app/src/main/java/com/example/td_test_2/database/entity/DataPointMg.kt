package com.example.td_test_2.database.entity

import com.example.td_test_2.naivebayes.data.splitWords

data class DataPointMg(
    val point : String,
    val values : List<String>
){
    val feature = point.splitWords().distinct().toList()
}