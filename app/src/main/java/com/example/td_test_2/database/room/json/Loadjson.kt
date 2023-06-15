package com.example.td_test_2.database.room.json

import android.content.Context
import android.util.Log
import com.example.td_test_2.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object Loadjson {
    fun loadSentenceJson(context: Context): JSONArray?{
        val builder = StringBuilder()
        val resources = context.resources.openRawResource(R.raw.kalimat4)
        val reader = BufferedReader(InputStreamReader(resources))
        var line : String?
        try {
            while (reader.readLine().also { line = it } != null){
                builder.append(line)
            }
            val json = JSONObject(builder.toString())
            return json.getJSONArray("kalimat")
        }catch (exception: IOException) {
            exception.printStackTrace()
        } catch (exception: JSONException) {
            exception.printStackTrace()
        }
        return null
    }

    fun loadPimaJson(context: Context): JSONArray?{
        val builder = StringBuilder()
        val resources = context.resources.openRawResource(R.raw.pima_db)
        val reader = BufferedReader(InputStreamReader(resources))
        var line : String?
        try {
            while (reader.readLine().also { line = it } != null){
                builder.append(line)
            }
            val json = JSONObject(builder.toString())
            return json.getJSONArray("pima")
        }catch (exception: IOException) {
            Log.d("loadJson",exception.message.toString())
            exception.printStackTrace()
        } catch (exception: JSONException) {
            Log.d("loadJson",exception.message.toString())
            exception.printStackTrace()
        }
        return null
    }

    fun loadTestJson(context: Context): JSONArray?{
        val builder = StringBuilder()
        val resources = context.resources.openRawResource(R.raw.testkalimat)
        val reader = BufferedReader(InputStreamReader(resources))
        var line : String?
        try {
            while (reader.readLine().also { line = it } != null){
                builder.append(line)
            }
            val json = JSONObject(builder.toString())
            return json.getJSONArray("kalimat")
        }catch (exception: IOException) {
            Log.d("loadJson",exception.message.toString())
            exception.printStackTrace()
        } catch (exception: JSONException) {
            Log.d("loadJson",exception.message.toString())
            exception.printStackTrace()
        }
        return null
    }

    fun loadTrainJson(context: Context): JSONArray?{
        val builder = StringBuilder()
        val resources = context.resources.openRawResource(R.raw.trainkalimat)
        val reader = BufferedReader(InputStreamReader(resources))
        var line : String?
        try {
            while (reader.readLine().also { line = it } != null){
                builder.append(line)
            }
            val json = JSONObject(builder.toString())
            return json.getJSONArray("kalimat")
        }catch (exception: IOException) {
            Log.d("loadJson",exception.message.toString())
            exception.printStackTrace()
        } catch (exception: JSONException) {
            Log.d("loadJson",exception.message.toString())
            exception.printStackTrace()
        }
        return null
    }

}

