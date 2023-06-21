package com.example.td_test_2.naivebayes.data

data class Input<C>(
    val text : String,
    val category : C
){
    val features = text.split("").toList()
}
