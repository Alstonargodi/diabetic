package com.example.td_test_2.utils

import java.text.NumberFormat
import java.util.Locale

object ConfussionMatrix {
    fun calculateConfussionMatrix(
        tp: Int,
        fp: Int,
        tn: Int,
        fn: Int,
    ): String {
        var accuracy = (tp + tn).toDouble() / (tp + tn + fp + fn).toDouble()
        var precission = (tp).toDouble() / (tp + fp).toDouble()
        var recall = (tp).toDouble() / (tp + fn).toDouble()
        var specifiy = (tn).toDouble() / (tn + tp).toDouble()
        var F1score = 2 * (recall * precission / recall + precission)

        val format: NumberFormat = NumberFormat.getPercentInstance(Locale.US)

        return "accuracy = ${format.format(accuracy)} \n" +
                "precission = ${format.format(precission)} \n" +
                "recall = ${format.format(recall)} \n" +
                "specify = ${format.format(specifiy)} \n" +
                "f1score = ${format.format(F1score)} \n" +
                "tp: $tp fp: $fp tn: $tn fn: $fn"
    }
}