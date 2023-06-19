package com.example.td_test_2.presentasion.chatactivity

import Classifier
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Data
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.fts_tes.Utils.PerformanceTime.StartTimer
import com.example.fts_tes.Utils.PerformanceTime.TimeElapsed
import com.example.fts_tes.Utils.Timeidf
import com.example.td_test_2.presentasion.chatactivity.adapter.ReceiveMessageItem
import com.example.td_test_2.presentasion.chatactivity.adapter.SendMessageItem
import com.example.td_test_2.R
import com.example.td_test_2.SearchResultsAdapter
import com.example.td_test_2.database.sqllite.DatabaseTable
import com.example.td_test_2.database.entity.Message
import com.example.td_test_2.databinding.ActivityChatBinding
import com.example.td_test_2.chat.preprocessing.PreProcessing.preprocessingKalimat
import com.example.td_test_2.naivebayes.data.Input
import com.example.td_test_2.database.Repository
import com.example.td_test_2.database.entity.WordEntity
import com.example.td_test_2.database.room.DbConfig
import com.example.td_test_2.reminder.TaskReminder
import com.example.td_test_2.reminder.TaskReminder.Companion.NOTIFICATION_Channel_ID
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.HashMap
import java.util.concurrent.TimeUnit

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
            var predictYesShort2 = "glukosa 250 tekanandarah 72 ketebalankulit 35 insulin 0 beratbadan 80.6 pedigree 0.627 umur 50 hamil 0 "
            var info = "Apa itu Diabetes ?"
            var info2 = "bagaimana gejala diabetes ?"
            var info3 = "Apa penyebab diabetes?"

            //input kalimat
            var inputText = predictNo

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
                var pattern = data.sentence.toRegex()
                var result = data.result.toLowerCase()
                if (result.contains(text) || pattern.containsMatchIn(text) && data.type == "info"){
                    setenceSelect(
                        data.type,
                        text,
                        data.sentence,
                        data.result
                    )
                }else if(result.contains(text)){
                    setenceSelect(
                        data.type,
                        text,
                        data.sentence,
                        data.result
                    )
                }else if(data.type == "predict"){
                    setenceSelect(
                        data.type,
                        text,
                        data.sentence,
                        data.result
                    )
                }else if (data.type == "reminder"){
                    setenceSelect(
                        data.type,
                        text,
                        data.sentence,
                        data.result
                    )
                }
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
                //todo 3.1 klasifikasi RF
                predictRf(
                    pregnan = preprocessing["hamil"].toString(),
                    glucose = preprocessing["glukosa"].toString(),
                    bloodPressure = preprocessing["tekanandarah"].toString(),
                    skin = preprocessing["ketebalankulit"].toString(),
                    insulin = preprocessing["insulin"].toString(),
                    bmi = preprocessing["beratbadan"].toString(),
                    pedigree = preprocessing["pedigree"].toString(),
                    age = preprocessing["umur"].toString()
                )
            }
            "reminder"->{
                //todo set alarm
                Log.d("chat_reminder", answer)
                replyMessage(answer)
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

        val pattern = Regex("(\\w+) ([+-]?([0-9]*[.])?[0-9]+)")

        val matches = pattern.findAll(predictText)
        val result = matches.map { matchResult ->
            val (word, value) = matchResult.destructured
            Pair(word, value)
        }.toMap()
        Log.d("result_patternpredict", result.toString())
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
        inputData.replace(".","")
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
        replyMessage("hasil naive bayes $message")
    }

    private fun predictRf(
        pregnan : String,
        glucose : String,
        bloodPressure : String,
        skin : String,
        insulin : String,
        bmi : String,
        pedigree : String,
        age : String,
    ){

        baseContext.deleteFile("amytextfile.txt")
        val fileOutputStream: FileOutputStream = openFileOutput("amytextfile.txt", Context.MODE_PRIVATE)
        val outputWriter = OutputStreamWriter(fileOutputStream)
        outputWriter.write((
                "${pregnan},"+
                        "${glucose},"+
                        "${bloodPressure},"+
                        "${skin},"+
                        "${insulin},"+
                        "${bmi},"+
                        "${pedigree},"+
                        "${age},"
                )
        )
        outputWriter.close()
        var result = randomforest.Input.main(this,"input",10)
        var decission = ""
        var hashresult = predictPreprocessing(result)
        if (hashresult["hasil"] == "1"){
            decission = "terkena diabetes"
        }else{
            decission = "tidak terkena diabetes"
        }

        var message = getString(R.string.pesanhasil_klasifikasi, decission, result)
        replyMessage("hasil random forest $message")
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

    private fun setIntervalTime(intervalTime : Long, activated : Boolean){
        val workManager = WorkManager.getInstance(this)
        val notificationBuilder = Data.Builder()
            .putString(NOTIFICATION_Channel_ID,"TaskReminderBroadcast")
            .build()
        val periodicAlarm = PeriodicWorkRequest.Builder(
            TaskReminder::class.java,
            intervalTime,
            TimeUnit.MILLISECONDS
        ).setInputData(notificationBuilder).build()

        if(activated){
            workManager.enqueue(periodicAlarm)
        }else{
            workManager.pruneWork()
            workManager.cancelAllWork()
        }
    }
}