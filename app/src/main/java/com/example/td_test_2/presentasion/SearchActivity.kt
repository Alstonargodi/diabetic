package com.example.td_test_2

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.td_ad.presentasion.adapter.ReceiveMessageItem
import com.example.td_test_2.chat.preprocessing.Tokenizer
import com.example.td_test_2.database.entity.Message
import com.example.td_test_2.database.entity.WordEntity
import com.example.td_test_2.database.sqldb.DatabaseTable
import com.example.td_test_2.databinding.ActivitySearchBinding

@Suppress("DEPRECATION")
class SearchActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySearchBinding

    private var mAdapter: SearchResultsAdapter? = null
//    private var mAsyncTask: SearchTask? = null
    private var mQuery = ""
    private var mToast: Toast? = null
    private var cleanText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Search data"
        binding.btSearchAction.setOnClickListener {
            var query = "saya memiliki tekanan darah 35 glukoas 30"

            //TODO 1.1 Preprocessing Kalimat
            query = query.replace("[.,]","")
            cleanText = query
            mQuery = query
//            if (mAsyncTask != null) {
//                Log.d("search_task", "cancel")
//            }

            //TODO 1.2 Calculate
            calculate(query)
        }
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvShowResults.layoutManager = linearLayoutManager
        mAdapter = SearchResultsAdapter(this)
        binding.rvShowResults.adapter = mAdapter
    }


    private fun calculate(
        params : String
    ){
        val cursor = DatabaseTable.getInstance(baseContext)?.getWordMatches(
            params, null,
            true,
            true,
            true
        ) // return null
        mAdapter?.swapCursor(cursor)
        mAdapter!!.onItemDetailCallback(object : SearchResultsAdapter.OnDetailItemCallback{
            override fun onDetailCallback(data: WordEntity) {
                Log.d("chat_data", data.type)
                extractSentence(data.type,data.result)
            }
        })
    }


    private fun extractSentence(
        type : String,
        answer : String,
    ){
        when(type){
            "info"->{
                Log.d("chat_info", answer)
            }
            "predict"->{
                var preprocessing = predictPreprocessing()

                var Bp = preprocessing.get("darah")

                Log.d("chat_predict", Bp.toString() )
            }
            "reminder"->{
                Log.d("chat_reminder", answer)
            }
            "help"->{
                Log.d("chat_help", answer)
            }
        }
    }

    private fun predictPreprocessing(): Map<String,Int>{
        var predictText = cleanText
            .replace("saya","")
            .replace("memiliki","")

        val pattern = Regex("(\\w+) (\\d+)")

        val matches = pattern.findAll(predictText)
        val result = matches.map { matchResult ->
            val (word, value) = matchResult.destructured
            Pair(word, value.toInt())
        }.toMap()

        return result
    }
    private fun showToast(message: String) {
        if (mToast != null) mToast!!.cancel()
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}