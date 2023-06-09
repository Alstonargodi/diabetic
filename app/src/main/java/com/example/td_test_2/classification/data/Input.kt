package com.example.td_test_2.classification.data

data class Input<C>(
    val text : String,
    val category : C
){
    val features = text.split("").distinct().toList()
}
