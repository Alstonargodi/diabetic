package com.example.td_test_2.presentasion

import Classifier
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fts_tes.Utils.PerformanceTime.StartTimer
import com.example.fts_tes.Utils.PerformanceTime.TimeElapsed
import com.example.fts_tes.Utils.Timeidf
import com.example.td_ad.presentasion.adapter.ChatAdapter
import com.example.td_ad.presentasion.adapter.ReceiveMessageItem
import com.example.td_ad.presentasion.adapter.SendMessageItem
import com.example.td_test_2.SearchResultsAdapter
import com.example.td_test_2.database.sqldb.DatabaseTable
import com.example.td_test_2.database.entity.Message
import com.example.td_test_2.databinding.ActivityChatBinding
import com.example.td_test_2.chat.preprocessing.PreProcessing.preprocessingKalimat
import com.example.td_test_2.classification.data.Input
import com.example.td_test_2.database.Repository
import com.example.td_test_2.database.entity.WordEntity
import com.example.td_test_2.database.room.DbConfig
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import java.util.Calendar

class ChatActivity : AppCompatActivity() {
    private val messageAdapter = GroupAdapter<GroupieViewHolder>()
    private var mAdapter: SearchResultsAdapter? = null
    private lateinit var repository: Repository
    private val classifier = Classifier<String>()

    private lateinit var binding : ActivityChatBinding
    private var time_o = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        repository = Repository(DbConfig.setDatabase(this))
        setContentView(binding.root)

        binding.rvChat.adapter = messageAdapter
        binding.btnInsert.setOnClickListener {

            //kalimat tester
            var reminder = "alarm atur pemberian buah setiap hari pada pukul 6 pagi"
            var predictYes = "prediksi riwayat kesahatan saya memiliki hamil 6 glukosa 148 tekanandarah 72 ketebalankulit 35 insulin 0 beratbadan 33.6 pedigree 0.627 umur 50"
            var predictNewHigh = "prediksi riwayat kesahatan saya memiliki hamil 0 glukosa 340 tekanandarah 72 ketebalankulit 35 insulin 5 beratbadan 53.6 pedigree 0.687 umur 55"
            var predictNo = "prediksi riwayat kesahatan saya memiliki hamil 1 glukosa 85 tekanandarah 66 ketebalankulit 29 insulin 0 beratbadan 26.6 pedigree 0.351 umur 31"
            var predictNewLow = "prediksi riwayat kesahatan saya memiliki hamil 0 glukosa 90 tekanandarah 52 ketebalankulit 35 insulin 5 beratbadan 53.6 pedigree 0.687 umur 55"
            var predictNoShort = "hamil 1 glukosa 85 tekanandarah 66 ketebalankulit 29 insulin 0 beratbadan 26.6 pedigree 0.351 umur 31"
            var predictYesShort = "hamil 0 glukosa 248 tekanandarah 72 ketebalankulit 35 insulin 0 beratbadan 80.6 pedigree 0.627 umur 50"
            var info = "Apa itu sakit Diabetes ?"
            var info2 = "bagaimana gejala diabetes ?"
            var info3 = "Apa penyebab diabetes?"

            //input kalimat
            var inputText = predictYesShort

            //todo preprocessing kalimat
            StartTimer()
            var cleanText = preprocessingKalimat(
                this,
                inputText
            )

            Log.d("proses",Timeidf.toastMessageNb)
            //mengirimkan data
            queryDatabase(cleanText)
            val message = Message(
                setences = inputText,
                sender = "me"
            )
            val sendMessageItem = SendMessageItem(message)
            messageAdapter.add(sendMessageItem)
            binding.etInsertChat.text.clear()

        }
        initPredictionDataset()
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvSearch.adapter = messageAdapter
        mAdapter = SearchResultsAdapter(this)
        binding.rvSearch.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.rvSearch.adapter = mAdapter
    }

    //mencari kesamaan kalimat dalam database
    private fun queryDatabase(
        text : String
    ){
        val searchDb = DatabaseTable.getInstance(baseContext)?.getWordMatches(
            text,
            null,
            true,
            true,
            true
        )
        mAdapter?.swapCursor(searchDb)
        mAdapter!!.onItemDetailCallback(object : SearchResultsAdapter.OnDetailItemCallback{
            override fun onDetailCallback(data: WordEntity) {
                setenceSelect(
                    data.type,
                    text,
                    data.result
                )
            }
        })
    }

    //memilah kalimat hasil tfidf berdasarkan tipe
    private fun setenceSelect(
        type : String,
        question : String,
        answer : String,
    ){
        when(type){
            "info"->{
                replyMessage(answer)
            }
            "predict"->{
                //preprocessing prediksi dari kalimat input
                var preprocessing = predictPreprocessing(question)
                predictNb(
                    pregnan = preprocessing["hamil"].toString(),
                    glucose = preprocessing["glukosa"].toString(),
                    bloodPreasure = preprocessing["tekanandarah"].toString(),
                    skin = preprocessing["ketebalankulit"].toString(),
                    insulin = preprocessing["insulin"].toString(),
                    bmi = preprocessing["beratbadan"].toString(),
                    pedigree = preprocessing["pedigree"].toString(),
                    age = preprocessing["umur"].toString()
                )
            }
            "reminder"->{
                Log.d("chat_reminder", answer)
            }
            "help"->{
                replyMessage(answer)
            }
        }
    }

    //preprocessing prediksi dengan key dan value
    private fun predictPreprocessing(
        predict : String
    ): Map<String,String>{
        var predictText = predict
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

    //inisialisasi dataset untuk prediksi
    private fun initPredictionDataset(){
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

    //prediksi utama
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
        Log.d("result_input",inputData)
        val yes = "6 148 72 35 0 336 0.627 50"
        val no = "1 85 66 29 0 266 0351 31"
        val new = "0 340 72 35 5 536 0687 55"

        var predict = classifier.predict(inputData)
        var result = "$inputData" +
                "\n relate ${predict["1"]}" +
                "\n not relate  ${ predict["0"] }"
        Log.d("result_nb",result)
        replyMessage(result)
    }

    //mengirimkan jawaban
    private fun replyMessage(
        sentence : String
    ){
        val receive = Message(
            setences = sentence,
            sender = "me"
        )
        val receiveItem = ReceiveMessageItem(receive)
        messageAdapter.add(receiveItem)
        val endTime = TimeElapsed()
        Log.d("time",endTime.toString())
    }
}