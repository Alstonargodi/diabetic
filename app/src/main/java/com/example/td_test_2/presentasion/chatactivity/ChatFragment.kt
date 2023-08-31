package com.example.td_test_2.presentasion.chatactivity

import Classifier
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fts_tes.Utils.PerformanceTime.StartTimer
import com.example.fts_tes.Utils.PerformanceTime.TimeElapsed
import com.example.td_test_2.presentasion.chatactivity.adapter.ReceiveMessageItem
import com.example.td_test_2.presentasion.chatactivity.adapter.SendMessageItem
import com.example.td_test_2.R
import com.example.td_test_2.SearchResultsAdapter
import com.example.td_test_2.database.sqllite.DatabaseTable
import com.example.td_test_2.database.entity.message.Message
import com.example.td_test_2.ml.preprocessing.PreProcessing.preprocessingKalimat
import com.example.td_test_2.database.Repository
import com.example.td_test_2.database.entity.task.TaskEntity
import com.example.td_test_2.database.entity.weightresult.WeightResult
import com.example.td_test_2.database.entity.words.WordEntity
import com.example.td_test_2.database.room.DbConfig
import com.example.td_test_2.database.room.json.Loadjson
import com.example.td_test_2.databinding.FragmentChatBinding
import com.example.td_test_2.ml.naivebayes.data.Input
import com.example.td_test_2.presentasion.ClasificationFormActivity
import com.example.td_test_2.presentasion.mainactivity.MainActivity
import com.example.td_test_2.utils.reminder.TaskReminderBroadcast
import com.example.td_test_2.utils.UtilsSetences
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import org.json.JSONException
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.time.LocalDateTime

class ChatFragment : Fragment() {
    private val messageAdapter = GroupAdapter<GroupieViewHolder>()
    private var mAdapter: SearchResultsAdapter? = null
    private lateinit var repository: Repository
    private val classifier = Classifier<String>()
    private lateinit var binding : FragmentChatBinding

    private var trainData = "pimall.csv"
    private var isMenu = false
    private var setReminder = false

    private var typeClassificer = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(layoutInflater)
        repository = Repository(DbConfig.setDatabase(requireContext()))

        repository.readSentence().observe(viewLifecycleOwner){
            if (it.isEmpty()){
                insertDataset(requireContext())
            }
        }
        repository.readSelectedAlgorithm().observe(viewLifecycleOwner){ algo->
            algo.forEach { typeClassificer=it.type }
        }
        //todo init reply
        replyMessage("Halo !")
        binding.rvChat.adapter = messageAdapter
        binding.btnMenu.setOnClickListener {
            startActivity(Intent(requireContext(),MainActivity::class.java))
        }
        binding.btnInsert.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            messageAdapter.clear()

            //input kalimat
            var inputText = binding.etInsertChat.text.toString()

            if(inputText.isNotEmpty()){

                try{
                    val message = Message(
                        setences = inputText,
                        sender = "me"
                    )
                    val sendMessageItem = SendMessageItem(message)
                    messageAdapter.add(sendMessageItem)
                    //todo 1.1 preprocessing kalimat
                    StartTimer()
                    var cleanText = preprocessingKalimat(
                        requireContext(),
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
                    if (inputText == "prediksi"){
                        startActivity(Intent(
                            requireContext(),
                            ClasificationFormActivity::class.java
                        ))
                    }
                    binding.etInsertChat.text.clear()
                }catch (e : Exception){
                    replyMessage("tidak dapat menemukan kaliamt terkait")
                }

            }else{
                replyMessage("teksboks kosong silahkan masukan kembali")
            }

        }
        initNbTrainData()
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext())

        binding.rvSearch.adapter = messageAdapter
        mAdapter = SearchResultsAdapter(requireContext())
        binding.rvSearch.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.rvSearch.adapter = mAdapter

        return binding.root
    }

    //mencari kesamaan kalimat dalam database
    private fun queryDatabase(
        questionInput : String
    ){
        //todo 1.3 start query
        val searchDb = DatabaseTable.getInstance(
            requireContext().applicationContext
        )?.getWordMatches(questionInput)

        //todo 1.6 mengambil data bedasarkan query dan proses tfidf
        mAdapter?.swapCursor(searchDb)
        if(searchDb?.count == 0){
            replyMessage("maaf respon tidak dapat ditemukan coba masukan kembali")
        }
        repository.deleteWightResult()
        mAdapter!!.onItemDetailCallback(object : SearchResultsAdapter.OnDetailItemCallback{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDetailCallback(data: WordEntity) {

                repository.insertWeightResult(WeightResult(
                    0,
                    data.type,
                    data.sentence,
                    data.result.toLowerCase()
                ))
                repository.readWeightResult().observe(viewLifecycleOwner){ list->
                    if (list.isNotEmpty()){
                        sortResult(
                            data.type,
                            questionInput,
                            questionInput,
                            data.result
                        )
                    }
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sortResult(
        type : String,
        MatchQuestion : String,
        question : String,
        answer : String,
    ){
        repository.sortWeightResult(MatchQuestion).observe(viewLifecycleOwner){data->
            repository.readWeightResult().observe(this){ list->
                list.distinct().forEach {
                    if(it.sentence == MatchQuestion){
                        setenceSelect(
                            it.type,
                            MatchQuestion,
                            it.result
                        )
                    }
                    if(data.isEmpty()){
                        setenceSelect(
                            it.type,
                            MatchQuestion,
                            it.result
                        )
                    }
                }
            }
        }
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
                replyMessageClasf(
                    "info",
                    question,
                    answer
                )
            }
            "predict"->{
                binding.progressBar.visibility = View.VISIBLE
                var preprocessing = predictPreprocessing(question)
                when(typeClassificer){
                    0->{
                        //todo 2.1 klasifikasi nb
                        predictNb(preprocessing)
                        //todo 3.1 klasifikasi RF
                        predictRf(preprocessing)
                    }
                    1->{
                        predictRf(preprocessing)
                    }
                    2->{
                        predictNb(preprocessing)
                    }

                }
            }
            "reminder"->{
                var pukul = reminderPreprocessing(question)
                var setence = "Mengatur pengingat"
                setReminder(pukul["pukul"].toString(),question)
                replyMessageClasf(
                    "reminder",
                    question,
                    setence
                )
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
            requireContext(),
            trainData,
        ).forEach { datapoint->
            classifier.apply {
                var input = datapoint.values.toString().replace(" ","")
                train(
                    Input(
                        input,
                        datapoint.point
                    )
                )
            }
        }
    }

    //prediksi utama
    private fun predictNb(
        preprocessing : Map<String,String>
    ){
        var pregnan = preprocessing["hamil"].toString()
        var glucose = preprocessing["glukosa"].toString()
        var bloodPreasure = preprocessing["tekanandarah"].toString()
        var skin = preprocessing["ketebalankulit"].toString()
        var insulin = preprocessing["insulin"].toString()
        var bmi = preprocessing["beratbadan"].toString()
        var pedigree = preprocessing["pedigree"].toString()
        var age = preprocessing["umur"].toString()

        val inputData = "$pregnan $glucose $bloodPreasure $skin $insulin $bmi $pedigree $age"

        //todo 2.2 start klasifikasi nb
        var predict = classifier.predict(inputData)
        var decission = ""

        decission = if (predict["1"]!! >= predict["0"]!!){
            "terkena diabetes"
        }else{
            "tidak terkena diabetes"
        }
        binding.progressBar.visibility = View.VISIBLE
        var message = getString(R.string.pesanhasil_klasifikasi, decission,"")
        replyMessageClasf(
            tipe ="predict",
            sentence = "${pregnan},"+
                    "${glucose},"+
                    "${bloodPreasure},"+
                    "${skin},"+
                    "${insulin},"+
                    "${bmi},"+
                    "${pedigree},"+
                    "${age},",
            result = "hasil naive bayes $message",
        )
    }

    private fun predictRf(
        preprocessing : Map<String,String>
    ){
        var pregnancies = preprocessing["hamil"].toString()
        var glucose = preprocessing["glukosa"].toString()
        var bloodPressure = preprocessing["tekanandarah"].toString()
        var skin = preprocessing["ketebalankulit"].toString()
        var insulin = preprocessing["insulin"].toString()
        var bmi = preprocessing["beratbadan"].toString()
        var pedigree = preprocessing["pedigree"].toString()
        var age = preprocessing["umur"].toString()

        requireContext().applicationContext.deleteFile("amytextfile.txt")

        val fileOutputStream: FileOutputStream = requireContext().openFileOutput(
            "amytextfile.txt", Context.MODE_PRIVATE
        )

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
            requireContext(),
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
        binding.progressBar.visibility = View.VISIBLE
        var message = getString(R.string.pesanhasil_klasifikasi, decission,"")
        replyMessageClasf(
            tipe ="predict",
            sentence = "${pregnancies},"+
                    "${glucose},"+
                    "${bloodPressure},"+
                    "${skin},"+
                    "${insulin},"+
                    "${bmi},"+
                    "${pedigree},"+
                    "${age},",
            result = "hasil random forest $message",
        )
        replyMessage("hasil naive bayes $message")
    }

    //mengirimkan jawaban
    private fun replyMessageClasf(
        tipe : String,
        sentence : String,
        result : String
    ){
        var hasilkalimat = if(sentence == "Halo !") "Halo! \n selamat datang di aplikasi diabetic" else "$result\n\nwaktu komputasi ${TimeElapsed().toString()}"
        val receive = Message(
            setences = hasilkalimat,
            sender = "me",
        )
        binding.progressBar.visibility = View.GONE
        repository.readSentence(sentence).observe(this@ChatFragment){ list->
            if(list.isEmpty()){
                insertNewData(tipe, sentence, result)
            }
        }

        val receiveItem = ReceiveMessageItem(receive)
        messageAdapter.add(receiveItem)
    }

    private fun replyMessage(
        sentence : String,
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
        TaskReminderBroadcast().setDailyReminder(requireContext(),200)
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
        TaskReminderBroadcast().cancelAlarm(requireContext())
    }

    private fun MenuResponse(
        inputText : String
    ){
        isMenu = true
        when(inputText){
            "1"->{
                val cinformasi = getString(R.string.cinformasi)
                val informasi = getString(R.string.informasi)
                val message = getString(R.string.messageInfo) + "$informasi \n\n$cinformasi \n"
                replyMessage(message)
            }
            "2"->{
                val cReminder = getString(R.string.creminder)
                val Reminder = getString(R.string.reminder)
                val message = getString(R.string.messageReminder) +
                        "$Reminder \n\n$cReminder \n"
                replyMessage(message)
            }
            "3"->{
                val cklasifikasi = getString(R.string.cKlasifikasi)
                val pklasifikasi = getString(R.string.pKlasifikasi)
                val klasifikasi = getString(R.string.klasifikasi)
                val message = getString(R.string.messageKlasifikasi) +
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
        repository.readTodayTask().observe(viewLifecycleOwner){
            it.forEach { task->
                replyMessage("${task.title}")
            }
        }
    }

    private fun insertDataset(
        context: Context,
    ){
        val db: DatabaseTable = DatabaseTable.getInstance(requireContext().applicationContext)!!
        val setenceArray = Loadjson.loadSentenceJson(context)
        try {
            if (setenceArray != null){
                for (i in 0 until setenceArray.length()){
                    val item = setenceArray.getJSONObject(i)

                    var inputCsv = item.getString("kalimat")
                    var cleanText = preprocessingKalimat(
                        requireContext(),
                        inputCsv
                    )

                    repository.insertSentence(
                        WordEntity(
                            id = 0,
                            type = item.getString("tipe"),
                            sentence = cleanText,
                            result = item.getString("result")
                        )
                    )

                    db.addNewEntry(
                        tipe = item.getString("tipe"),
                        pattern = cleanText,
                        answer = item.getString("result")
                    )
                }
            }
            addToast("prepopulate data berhasil")
        }catch (e : JSONException){
            Log.d("roomDb",e.message.toString())
            e.printStackTrace()
        }
    }
    private fun addToast(message: String) {
        Toast.makeText(requireContext().applicationContext, message, Toast.LENGTH_LONG).show()
    }


    private fun insertNewData(
        tipe : String,
        setence : String,
        result : String
    ){
        val db: DatabaseTable = DatabaseTable.getInstance(requireContext().applicationContext)!!

        repository.insertSentence(
            WordEntity(
                id = 0,
                type = tipe,
                sentence = setence,
                result = result
            )
        )

        db.addNewEntry(
            tipe = tipe,
            pattern = setence,
            answer = result
        )
    }
}