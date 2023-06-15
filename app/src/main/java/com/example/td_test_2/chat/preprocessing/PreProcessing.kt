package com.example.td_test_2.chat.preprocessing

import android.content.Context
import android.util.Log
import com.example.fts_tes.Utils.PerformanceTime
import com.example.fts_tes.Utils.Timeidf
import java.util.Calendar

object PreProcessing {

    fun preprocessingKalimat(
        context : Context,
        setence: String
    ): String{
        //todo tokenizer
        var kalimatToken = sentenceToToken(setence)

        //todo capital removal
        var capitalRemoval = captialRemoval(kalimatToken)

        //todo removeline
        var removeLine = removeLineBreaks(capitalRemoval.toString())

        Log.d("calctime_preprocessing", PerformanceTime.TimeElapsed().toString())
        return  removeLine
    }

    fun sentenceToToken(text : String): List<String>{
        val setence = text.trim()
        var tokens = setence.split(" ")
//        tokens = tokens.map {
//            Regex("[^A-Za-z0-9 ]").replace( it , "")
//        }
        tokens = tokens.filter {
            it.trim().isNotEmpty() and it.trim().isNotBlank()
        }
        Log.d("calctime_tokenisation", PerformanceTime.TimeElapsed().toString())
        return tokens
    }

    fun captialRemoval(setence : List<String>): List<String> {
        var setenceList = arrayListOf<String>()
        setence.forEach {
            setenceList.add(it.toLowerCase())
        }
        Log.d("calctime_capitalremoval", PerformanceTime.TimeElapsed().toString())
        return setenceList
    }

    fun removeStopWords(setence : List<String>,context: Context): String {
        var stopword = Utils.csvToString(
            context,
            "stopwordbahasa.csv",
            false
        )
        var words = setence.filter {
            !stopword.contains( it.trim() )
        }
        return words.toString()
    }

    fun removeLineBreaks( setence : String ) : String {
        val cleaning = setence
            .replace("\n", " " )
            .replace("\r", " " )
            .replace("?", " " )
            .replace(",", " " )
            .replace("[", " " )
            .replace("]", " " )
            .replace("  ", " " )
            .replaceFirst(" ","")
            .replaceAfterLast(" ","")
        Log.d("calctime_capitalremoval", PerformanceTime.TimeElapsed().toString())
        return cleaning
    }
}