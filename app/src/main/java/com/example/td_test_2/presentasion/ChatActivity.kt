package com.example.td_test_2.presentasion

import Classifier
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fts_tes.Utils.PerformanceTime
import com.example.fts_tes.Utils.PerformanceTime.StartTimer
import com.example.fts_tes.Utils.PerformanceTime.TimeElapsed
import com.example.fts_tes.Utils.Timeidf
import com.example.td_ad.presentasion.adapter.ChatAdapter
import com.example.td_ad.presentasion.adapter.ReceiveMessageItem
import com.example.td_ad.presentasion.adapter.SendMessageItem
import com.example.td_test_2.R
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
import patternmatching.BooyerMoore
import java.util.Calendar
import java.util.HashMap

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

        //todo init reply
        replyMessage("Halo !")
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
            var inputText = info

            //todo 1.1 preprocessing kalimat
            StartTimer()
            var cleanText = preprocessingKalimat(
                this,
                inputText
            )

            Log.d("proses",Timeidf.toastMessageNb)
            //todo 1.2 query kalimat
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
        var queryList = arrayListOf<WordEntity>()
        var hashList = HashMap<String,String>()
        //todo 1.3 start query
        val searchDb = DatabaseTable.getInstance(baseContext)?.getWordMatches(
            text,
            null,
            true,
            true,
            true
        )
        //todo 1.6 mengambil data bedasarkan query dan proses tfidf
        mAdapter?.swapCursor(searchDb)
        mAdapter!!.onItemDetailCallback(object : SearchResultsAdapter.OnDetailItemCallback{
            override fun onDetailCallback(data: WordEntity) {
//                var test = data.sentence.toRegex()
//                if (test.containsMatchIn(text)){
//                    Log.d("chat",data.sentence)
//                }

                setenceSelect(
                    data.type,
                    text,
                    data.sentence,
                    data.result
                )
            }
        })
    }

    //memilah kalimat hasil tfidf berdasarkan tipe
    private fun setenceSelect(
        type : String,
        question : String,
        patternFound : String,
        answer : String,
    ){
        //todo 1.17 berdasarkan pattern tertinggi kemudian dikategorikan
        when(type){
            "info"->{
                val message = "berikut ini hasil dari pertanyaan anda \n\n$answer"
                replyMessage(message)
            }
            "predict"->{
                var preprocessing = predictPreprocessing(question)
                //todo 2.1 klasifikasi nb
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
        //todo 2.3 init data latih nb
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

        //todo 2.2 start klasifikasi nb
        var predict = classifier.predict(inputData)
        var decission = ""
        var result = "$inputData \n" +
                "ya ${predict["1"]} \n" +
                "tidak  ${ predict["0"] }"
        if (predict["1"]!! >= predict["0"]!!){
            decission = "terkena diabetes"
        }else{
            decission = "tidak terkena diabetes"
        }

        var message = getString(R.string.pesanhasil_klasifikasi, decission, result)
        replyMessage(message)
    }

    //mengirimkan jawaban
    private fun replyMessage(
        sentence : String
    ){
        val receive = Message(
            setences = "$sentence\n\nwaktu komptuasi ${TimeElapsed().toString()}",
            sender = "me",
        )
        val receiveItem = ReceiveMessageItem(receive)
        messageAdapter.add(receiveItem)
        Log.d("calctime_replymessage", TimeElapsed().toString())
    }
}