package com.example.td_test_2

import Classifier
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.td_test_2.classification.data.Input
import com.example.td_test_2.database.Repository
import com.example.td_test_2.database.entity.WordEntity
import com.example.td_test_2.database.room.DbConfig
import com.example.td_test_2.database.sqldb.DatabaseTable
import com.example.td_test_2.databinding.ActivitySearchBinding

@Suppress("DEPRECATION")
class SearchActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySearchBinding
    private lateinit var repository: Repository

    private var mAdapter: SearchResultsAdapter? = null
//    private var mAsyncTask: SearchTask? = null
    private var mQuery = ""
    private var mToast: Toast? = null
    private var cleanText = ""

    private val classifier = Classifier<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = Repository(DbConfig.setDatabase(this))

        title = "Search data"
        binding.btSearchAction.setOnClickListener {
            var predict2 = "prediksi riwayat kesahatan saya memiliki hamil 6 glukosa 148 darah 72 ketebalan 35 insulin 0 berat 33.6 pedigree 0.627 umur 50"
            var predict = "prediksi riwayat kesahatan saya memiliki hamil 0 glukosa 340 darah 72 ketebalan 35 insulin 5 berat 53.6 pedigree 0.687 umur 55"
            var query = predict2

            //TODO 1.1 Preprocessing Kalimat
//            query = query.replace("[,]","")
            cleanText = query
            mQuery = query

            //TODO 1.2 Calculate
            calculate(query)
        }
        initializeNb()
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
        Log.d("result_type",type)
        when(type){
            "info"->{
                Log.d("chat_info", answer)
            }
            "predict"->{
                var preprocessing = predictPreprocessing()
                predictNb(
                    pregnan = preprocessing["hamil"].toString(),
                    glucose = preprocessing["glukosa"].toString(),
                    bloodPreasure = preprocessing["darah"].toString(),
                    skin = preprocessing["ketebalan"].toString(),
                    insulin = preprocessing["insulin"].toString(),
                    bmi = preprocessing["berat"].toString(),
                    pedigree = preprocessing["pedigree"].toString(),
                    age = preprocessing["umur"].toString()
                )
            }
            "reminder"->{
                Log.d("chat_reminder", answer)
            }
            "help"->{
                Log.d("chat_help", answer)
            }
        }
    }

    private fun predictPreprocessing(): Map<String,String>{
        var predictText = cleanText
            .replace("saya","")
            .replace("memiliki","")

        val pattern = Regex("(\\w+) (\\d+)")

        val matches = pattern.findAll(predictText)
        val result = matches.map { matchResult ->
            val (word, value) = matchResult.destructured
            Pair(word, value)
        }.toMap()

        return result
    }

    private fun initializeNb(){
        repository.readPimaData().observe(this){
            it.forEach {
                classifier.apply {
                    train(
                        Input(
                            "${it.pregnan}" +
                                    "${it.glucose}" +
                                    "${it.bloodPressure}" +
                                    "${it.skinThich}" +
                                    "${it.insulin}" +
                                    "${it.bmi}" +
                                    "${it.pedigree}" +
                                    "${it.age}",
                            it.outcome
                        )
                    )
                }
            }
        }
    }

    private fun predictNb(
        pregnan : String,
        glucose : String,
        bloodPreasure : String,
        skin : String,
        insulin : String,
        bmi : String,
        pedigree : String,
        age : String,
    ){
        val inputData = "$pregnan $glucose $bloodPreasure $skin $insulin $bmi $pedigree $age"
        val yes = "6 148 72 35 0 336 0.627 50"
        val no = "1 85 66 29 0 266 0351 31"
        val new = "0 340 72 35 5 536 0687 55"

        var predict = classifier.predict(inputData)
        binding.tvResult.text = "$inputData" +
                "\n relate ${predict["1"]}" +
                "\n not relate  ${ predict["0"] }"
    }

    private fun showToast(message: String) {
        if (mToast != null) mToast!!.cancel()
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}