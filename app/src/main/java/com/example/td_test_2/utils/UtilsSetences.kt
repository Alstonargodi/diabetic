package com.example.td_test_2.utils

import android.content.Context
import com.example.td_test_2.database.entity.DataPointMg
import java.io.BufferedReader
import java.io.InputStreamReader

object UtilsSetences {
    fun csvToStringI(
        context : Context,
        path : String,
    ): List<Instance> {
        val shift = 0

        BufferedReader(InputStreamReader(context.assets.open(path))).use { value ->
            val dataInput : MutableList<Instance> = arrayListOf()
            var input = value.readLine()
            val attributes = input.split(",".toRegex()).toTypedArray()
            while (value.readLine().also { input = it } != null){
                val sample = input!!.split(",".toRegex()).toTypedArray()
                val value = mutableListOf<String>()
                for (i in shift until sample.size-1 ){
                    value.add(sample[i])
                }
                dataInput.add(Instance(value,sample[sample.size-1]))
            }
            return dataInput
        }
    }

    fun csvToString2(
        context : Context,
        path : String,
    ): List<DataPointMg> {
        val shift = 0
        BufferedReader(InputStreamReader(context.assets.open(path))).use { value ->

            val dataInput : MutableList<DataPointMg> = arrayListOf()
            var input = value.readLine()
            val attributes = input.split(",".toRegex()).toTypedArray()
            while (value.readLine().also { input = it } != null){
                val sample = input!!.split(",".toRegex()).toTypedArray()
                val value = mutableListOf<String>()
                for (i in shift until sample.size-1 )
                    value.add(sample[i])
                dataInput.add(DataPointMg(sample[sample.size-1],value))
            }
            return dataInput
        }
    }
}