package com.example.td_test_2.presentasion.chatactivity

import Classifier
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fts_tes.Utils.PerformanceTime.StartTimer
import com.example.fts_tes.Utils.PerformanceTime.TimeElapsed
import com.example.td_test_2.presentasion.chatactivity.adapter.ReceiveMessageItem
import com.example.td_test_2.presentasion.chatactivity.adapter.SendMessageItem
import com.example.td_test_2.R
import com.example.td_test_2.SearchResultsAdapter
import com.example.td_test_2.database.sqllite.DatabaseTable
import com.example.td_test_2.database.entity.message.Message
import com.example.td_test_2.databinding.ActivityChatBinding
import com.example.td_test_2.chat.preprocessing.PreProcessing.preprocessingKalimat
import com.example.td_test_2.database.Repository
import com.example.td_test_2.database.entity.task.TaskEntity
import com.example.td_test_2.database.entity.words.WordEntity
import com.example.td_test_2.database.room.DbConfig
import com.example.td_test_2.presentasion.mainactivity.MainActivity
import com.example.td_test_2.reminder.TaskReminderBroadcast
import com.example.td_test_2.utils.UtilsSetences
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import patternmatching.boyerMooreHorspoolSearch
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.time.LocalDateTime

class ChatActivity : AppCompatActivity() {
    private val messageAdapter = GroupAdapter<GroupieViewHolder>()
    private var mAdapter: SearchResultsAdapter? = null
    private lateinit var repository: Repository
    private val classifier = Classifier<String>()
    private lateinit var binding : ActivityChatBinding

    private var trainData = "pimall.csv"
    private var isMenu = false
    private var setReminder = false
    private var setClassifier = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        repository = Repository(DbConfig.setDatabase(this))
        setContentView(binding.root)

        //todo init reply
        replyMessage("Halo !")
        binding.rvChat.adapter = messageAdapter
        binding.btnMenu.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
        }
        binding.btnInsert.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            //kalimat tester
            var reminder = "makan pukul 20.00"
            var reminderShort = "olahraga pada pukul 8 pagi besok !"
            var predictYes = "prediksi riwayat kesahatan saya memiliki hamil 6 glukosa 148 tekanandarah 72 ketebalankulit 35 insulin 0 beratbadan 33.6 pedigree 0.627 umur 50"
            var predictNewHigh = "prediksi riwayat kesahatan saya memiliki hamil 0 glukosa 340 tekanandarah 72 ketebalankulit 35 insulin 5 beratbadan 53.6 pedigree 0.687 umur 55"
            var predictNo = "prediksi riwayat kesahatan saya memiliki hamil 1 glukosa 85 tekanandarah 66 ketebalankulit 29 insulin 0 beratbadan 26.6 pedigree 0.351 umur 31"
            var predictNewLow = "prediksi riwayat kesahatan saya memiliki hamil 0 glukosa 90 tekanandarah 52 ketebalankulit 35 insulin 5 beratbadan 53.6 pedigree 0.687 umur 55"
            var predictNoShort = "hamil 0 glukosa 85 tekanandarah 66 ketebalankulit 29 insulin 0 beratbadan 26.6 pedigree 0.351 umur 31"
            var predictYesShort = "hamil 0 glukosa 248 tekanandarah 72 ketebalankulit 35 insulin 0 beratbadan 80.6 pedigree 0.627 umur 50"
            var predictYesShort2 = "glukosa 250 tekanandarah 72 ketebalankulit 35 insulin 0 beratbadan 80.6 pedigree 0.627 umur 50 hamil 0 "
            var predicyes3 = "glukosa 197 tekanandarah 74 ketebalankulit 45 insulin 0 beratbadan 43.6 pedigree 0.627 umur 54 hamil 0"
            var info = "Apa itu Diabetes?"
            var info2 = "bagaimana gejala diabetes ?"
            var info3 = "Apa penyebab diabetes?"
            var info4 = "apa itu stroke?"

            //input kalimat
            var inputText = binding.etInsertChat.text.toString()

            val message = Message(
                setences = inputText,
                sender = "me"
            )
            val sendMessageItem = SendMessageItem(message)
            messageAdapter.add(sendMessageItem)
            //todo 1.1 preprocessing kalimat
            StartTimer()
            var cleanText = preprocessingKalimat(
                this,
                inputText
            )
            //todo 1.2 query kalimat
            if(isMenu){
                MenuResponse(inputText)
            }else{
                queryDatabase(cleanText)
            }

            if (setReminder){
                setReminderClassifer(inputText)
            }
            binding.etInsertChat.text.clear()
        }
        initNbTrainData()
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
        questionInput : String
    ){
        //todo 1.3 start query
        val searchDb = DatabaseTable.getInstance(baseContext)?.getWordMatches(
            questionInput,
            null,
            true,
            true,
            true
        )
        //todo 1.6 mengambil data bedasarkan query dan proses tfidf
        mAdapter?.swapCursor(searchDb)
        if(searchDb?.count == 0){
            replyMessage("maaf respon tidak dapat ditemukan coba masukan kembali")
        }
        mAdapter!!.onItemDetailCallback(object : SearchResultsAdapter.OnDetailItemCallback{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDetailCallback(data: WordEntity) {
                var pattern = data.sentence.toRegex()
                var result = data.result.toLowerCase().toRegex()
                var match = boyerMooreHorspoolSearch(data.sentence,questionInput)
                setenceSelect(
                    data.type,
                    questionInput,
                    data.result
                )
            }
        })
    }

    //memilah kalimat hasil tfidf berdasarkan tipe
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setenceSelect(
        type : String,
        question : String,
        answer : String,
    ){
        //todo 1.17 berdasarkan pattern tertinggi kemudian dikategorikan
        when(type){
            "info"->{
                val message = "berikut ini hasil dari pertanyaan anda \n\n$answer"
                replyMessage(message)
            }
            "predict"->{
                binding.progressBar.visibility = View.VISIBLE
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
                    pregnancies = preprocessing["hamil"].toString(),
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
                var pukul = reminderPreprocessing(question)
                var setence = "Mengatur pengingat \n$answer pada pukul ${pukul["pukul"].toString()}"
                setReminder(pukul["pukul"].toString(),question)
                replyMessage(setence)
            }
            "help"->{
                val message = getString(R.string.menu_chat)
                replyMessage(message)
                replyMessage(answer)
                isMenu = true
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
        return result
    }

    private fun reminderPreprocessing(
        reminder : String
    ): Map<String,String>{
        var reminderText = reminder
        val pattern = Regex("(\\w+) ([+-]?([0-9]*[.])?[0-9]+)")

        val matches = pattern.findAll(reminderText)
        val result = matches.map { matchResult ->
            val (word, value) = matchResult.destructured
            Pair(word, value)
        }.toMap()
        return result
    }

    //todo 2.3 inisialisasi dataset untuk prediksi
    private fun initNbTrainData(){
        UtilsSetences.csvToString2(
            this,
            trainData,
        ).forEach { datapoint->
            classifier.apply {
                var input = datapoint.values.toString().replace(" ","")
                train(com.example.td_test_2.naivebayes.data.Input(
                    input,
                    datapoint.point
                )
                )
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

        decission = if (predict["1"]!! >= predict["0"]!!){
            "terkena diabetes"
        }else{
            "tidak terkena diabetes"
        }

        var message = getString(R.string.pesanhasil_klasifikasi, decission,"")
        replyMessage("hasil naive bayes $message")
    }

    private fun predictRf(
        pregnancies : String,
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
                "${pregnancies},"+
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
        var result = randomforest.Input.main(
            this,
            "input",
            trainData,
            5,
        )
        var decission = ""
        var hashresult = predictPreprocessing(result)
        if (hashresult["hasil"] == "1"){
            decission = "terkena diabetes"
            replyMessage("apakah anda ingin mengatur perawatan ?" +
                    "1.ya \n" +
                    "2.tidak"
            )
            setReminder = true
        }else{
            decission = "tidak terkena diabetes"
        }

        var message = getString(R.string.pesanhasil_klasifikasi, decission,"")
        replyMessage("hasil random forest $message")
    }

    //mengirimkan jawaban
    private fun replyMessage(
        sentence : String
    ){
        var setence = if(sentence == "Halo !") "Halo! \n selamat datang di aplikasi diabetic" else "$sentence\n\nwaktu komputasi ${TimeElapsed().toString()}"
        val receive = Message(
            setences = setence,
            sender = "me",
        )
        binding.progressBar.visibility = View.GONE
        val receiveItem = ReceiveMessageItem(receive)
        messageAdapter.add(receiveItem)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setReminder(
        time : String,
        description : String
    ){
        TaskReminderBroadcast().setDailyReminder(this,200)
        val date = LocalDateTime.now().dayOfMonth
        val month = LocalDateTime.now().month.value
        val year = LocalDateTime.now().year
        repository.deleteTodoTask(description)
        repository.insertTodoList(
            TaskEntity(
                0,
                description,
                2L,
                "24",
                year,
                month,
                date,
                time,
                "2",
                false,
                false
            )
        )
    }

    private fun setReminderClassifer(
        answer : String
    ){
        if (answer == "1"){
            setReminder = false
            replyMessage("masukan deksripsi kegiatan")
        }
    }

    private fun removeReminder(){
        replyMessage("menghapus alarm")
        repository.deleteTask()
        TaskReminderBroadcast().cancelAlarm(this)
    }

    private fun MenuResponse(
        inputText : String
    ){
        isMenu = true
        when(inputText){
            "1"->{
                val cinformasi = "Apa itu sakit diabetes dan bagaimana gejala diabetes ?"
                val informasi =
                    "Informasi terkait diabetes dapat diakses dengan kata kunci info atau memberikan pertanyaan seperti"
                val message = "berikut ini bantuan untuk kata kunci chat \n" +
                        "$informasi \n\n$cinformasi \n"
                replyMessage(message)
            }
            "2"->{
                val cReminder = "makan buah pukul 20.00"
                val Reminder = "Anda dapat mengatur penjadwalan kegiatan, seperti"
                val message = "berikut ini bantuan untuk kata kunci chat \n" +
                        "$Reminder \n\n$cReminder \n"
                replyMessage(message)
            }
            "3"->{
                val cklasifikasi = "glukosa 248 tekanandarah 74 ketebalankulit 45 insulin 0 beratbadan 43.6 pedigree 0.627 umur 54 hamil 0"
                val pklasifikasi =
                    "glukosa [jumlahglukosa] tekanandarah [jumlah tekanan darah] ketebalankulit [ketebalan kulit] insulin [jumlah pemakaian insulin] beratbadan [beratbadan] pedigree [nilai pedigree] umur [umur] hamil [jumlah kehamilan]"
                val klasifikasi =
                    "klasifikasi diabetes dapat dilakukan degnan memberikan riwayat keseahtan dengan menggunakan beberapa kata kunci,sebagai contoh"
                val message = "berikut ini bantuan untuk kata kunci chat \n" +
                        "$klasifikasi \n\n$pklasifikasi \n\n" +
                        "$cklasifikasi"
                replyMessage(message)
            }
            "4"->{
                showActivityList()
            }
            "cancel alarm"->{
                removeReminder()
                replyMessage("berhasil membatalkan alarm")
            }
            else->{
                replyMessage("tidak ditemukan silahkan masukan kembali")
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun showActivityList(){
        replyMessage("Berikut ini daftar kegiatan:")
        repository.readTodayTask().observe(this){
            it.forEach { task->
                replyMessage("${task.title}")
            }
        }

    }
}