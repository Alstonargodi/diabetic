package com.example.td_test_2.classification.data

fun String.splitWords(): Sequence<String>{
//    val stopWords = javaClass
//        .getResource("stopwords")
//        ?.readText()
//        ?.split("\n")
//        ?.toSet() ?: setOf()
    return split(Regex("\\s")).asSequence()
        .map { it.replace(Regex("[^A-Za-z]"), "").toLowerCase() }
//        .filter { it !in stopWords }
        .filter { it.isNotEmpty() }
}