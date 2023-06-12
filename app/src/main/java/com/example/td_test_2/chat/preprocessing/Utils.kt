package com.example.td_test_2.chat.preprocessing

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Calendar

object Utils {
    fun csvToString(
        context : Context,
        path : String,
        hasId : Boolean
    ): List<String> {
        val shift = 0

        BufferedReader(InputStreamReader(context.assets.open(path))).use { value ->
            val dataInput : MutableList<String> = arrayListOf()
            var input = value.readLine()
            val attributes = input.split(",".toRegex()).toTypedArray()
            while (value.readLine().also { input = it } != null){
                val sample = input!!.split(",".toRegex()).toTypedArray()
                val value = HashMap<String,Any?>()
                for (i in 0 until sample.size){
                    dataInput.add(sample[i])
                }
            }
            return dataInput
        }
    }
}
