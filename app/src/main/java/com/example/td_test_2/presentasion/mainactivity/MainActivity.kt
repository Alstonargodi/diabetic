package com.example.td_test_2.presentasion.mainactivity

import Classifier
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.td_test_2.SearchResultsAdapter
import com.example.td_test_2.ml.preprocessing.PreProcessing
import com.example.td_test_2.ml.tfidfmain.TfIdfMain
import com.example.td_test_2.database.Repository
import com.example.td_test_2.database.entity.algorithm.Algortihm
import com.example.td_test_2.database.entity.task.TaskEntity
import com.example.td_test_2.database.entity.testing.TestingNv
import com.example.td_test_2.database.entity.testing.TestingRf
import com.example.td_test_2.database.room.DbConfig
import com.example.td_test_2.database.room.json.Loadjson
import com.example.td_test_2.database.sqllite.DatabaseTable
import com.example.td_test_2.databinding.ActivityMainBinding
import com.example.td_test_2.presentasion.testingalgorithmactivity.TestingAlgorithmActivity
import com.example.td_test_2.presentasion.chatactivity.ChatActivity
import com.example.td_test_2.presentasion.enterdataactivity.EnterDataActivity
import com.example.td_test_2.presentasion.mainactivity.adapter.TestingResultAdapter
import com.example.td_test_2.presentasion.searchactivity.SearchActivity
import com.example.td_test_2.presentasion.viewdataactivity.ViewDataActivity
import com.example.td_test_2.utils.reminder.TaskReminder
import com.example.td_test_2.utils.reminder.TaskReminder.Companion.NOTIFICATION_Channel_ID
import com.example.td_test_2.utils.UtilsSetences
import com.example.td_test_2.utils.UtilsSetences.csvToString2
import org.json.JSONException
import randomforest.Input
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.NumberFormat
import java.time.LocalDateTime
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: Repository
    private lateinit var trainingAdapter : TestingResultAdapter
    private var mAdapter: SearchResultsAdapter? = null
    private var resultList = arrayListOf<String>()
    //    private var mAsyncTask: SearchTask? = null

    private var mQuery = ""
    private var mToast: Toast? = null
    private var cleanText = ""
    private var mCursor: Cursor? = null
    private var offset: IntArray? = null
    private var timeCompute = ""
    private var classifier = Classifier<String>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        repository = Repository(DbConfig.setDatabase(this))

        setContentView(binding.root)
        readAccuracy()

        binding.apply {
            btEnterData.setOnClickListener {
                val intent = Intent(this@MainActivity, EnterDataActivity::class.java)
                startActivity(intent)
            }
            btSearchData.setOnClickListener {
                val intent = Intent(this@MainActivity, SearchActivity::class.java)
                startActivity(intent)
            }
            btViewData.setOnClickListener {
                val intent = Intent(this@MainActivity, ViewDataActivity::class.java)
                startActivity(intent)
            }
            btnTest.setOnClickListener {
//                testingTfIdf()
//                testingRf()
//                repository.deleteTestingRfResult()
//                repository.deleteTestingNvResult()
//                testClassification()
                startActivity(Intent(this@MainActivity, TestingAlgorithmActivity::class.java))
            }
            btnSchat.setOnClickListener {
                startActivity(Intent(this@MainActivity, ChatActivity::class.java))
            }
            spnAlgoritm.onItemSelectedListener = object : AdapterView.OnItemClickListener,AdapterView.OnItemSelectedListener{
                override fun onItemClick(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when(parent?.getItemAtPosition(position)){
                        "keduaalgoritma"-> {
                            insertAlgorithm(0)
                        }
                        "randomforest"->{
                            insertAlgorithm(1)
                        }
                        "naivebayes"->{
                            insertAlgorithm(2)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    insertAlgorithm(0)
                }
            }
        }
    }


    fun testingTfIdf(){
        val setence = Loadjson.loadTestJson(this)
        var data = arrayListOf<String>()
        try {
            if (setence != null){
                for (i in 0 until setence.length()){
                    val item = setence.getJSONObject(i)
                    var cleanText = PreProcessing.preprocessingKalimat(
                        this,
                        item.getString("sentence")
                    )
                    data.add(cleanText)
                    queryDatabase(
                        item.getString("sentence"),
                        item.getString("type"),
                        item.getString("result")
                    )
                }
            }
        }catch (exception: IOException) {
            exception.printStackTrace()
        } catch (exception: JSONException) {
            exception.printStackTrace()
        }
    }


    private fun queryDatabase(
        text : String,
        typeTest : String,
        responseTest : String
    ){
        var testList = arrayListOf<String>()
        var cleanText = PreProcessing.preprocessingKalimat(
            this,
            text
        )
        testList.add(cleanText)
        for(i in 0 until testList.size){
            mAdapter = SearchResultsAdapter(this)
            val searchDb = DatabaseTable.getInstance(baseContext)?.getWordMatches(
                testList[i],
            )
            swapCursor(searchDb)
            if (offset != null) {
                mCursor!!.moveToPosition(offset!![i])
            } else {
                mCursor!!.moveToPosition(i)
            }
            @SuppressLint("Range")
            val type = mCursor!!.getString(mCursor!!.getColumnIndex(DatabaseTable.COL_TIPE))

            @SuppressLint("Range")
            val response = mCursor!!.getString(mCursor!!.getColumnIndex(DatabaseTable.COL_ANSWER))

            Log.d("AA${type}",testList[i])
            Log.d("AA${type}", if(type == typeTest) "true" else "false")
            Log.d("AA${type}", if(response == responseTest) "true" else "false")
            var result = "Q:${testList[i]} " +
                    "\ntypecorrect: ${if(type == typeTest) "true" else "false"} " +
                    "\nresponsecorrect: ${if(response == responseTest) "true" else "false"} " +
                    "\nreseponse" +
                    "\n$responseTest"
            resultList.add(result)
        }
//        showTestResult(resultList)
    }

    private fun swapCursor(newCursor: Cursor?) {
        offset = null
        mCursor = newCursor

        //todo 1.7 proses perhitungan TFIDF
        offset = TfIdfMain.calcTfIdf(this, mCursor)
        timeCompute = Input.timeCompute
    }

    private fun testClassification(){
        val dataInput = UtilsSetences.csvToStringI(
            this,
            "testing/pimakalimat_test_10.csv"
        )
        csvToString2(
            this,
            "pima/pima_train_30nr.csv",
        ).forEach { datapoint->
            classifier.apply {
                var input = datapoint.values.toString().replace(" ","")
                train(
                    com.example.td_test_2.ml.naivebayes.data.Input(
                        input,
                        datapoint.point
                    )
                )
                Log.d("NBdata", input)
            }
        }

        dataInput.forEach {
            readDatabase(it.features,it.label)
        }
    }


    private fun readDatabase(
        setence: List<String>,
        label : String,
    ){
        var position = 0
        for(i in 0 until setence.size){
            mAdapter = SearchResultsAdapter(this)
            val searchDb = DatabaseTable.getInstance(baseContext)?.getWordMatches(
                setence[i],
            )
            swapCursor(searchDb)
            position = if (null == mCursor) 0 else mCursor!!.count-1
            if (offset != null) {
                mCursor!!.moveToPosition(offset!![position])
            } else {
                mCursor!!.moveToPosition(position)
            }
            @SuppressLint("Range") val type = mCursor!!.getString(mCursor!!.getColumnIndex(DatabaseTable.COL_TIPE))
            @SuppressLint("Range") val response = mCursor!!.getString(mCursor!!.getColumnIndex(DatabaseTable.COL_ANSWER))
            if (type == "predict"){
               var preprocessing =  predictPreprocessing(setence.toString())
                predictRf(
                    pregnan = preprocessing["hamil"].toString(),
                    glucose = preprocessing["glukosa"].toString(),
                    bloodPressure = preprocessing["tekanandarah"].toString(),
                    skin = preprocessing["ketebalankulit"].toString(),
                    insulin = preprocessing["insulin"].toString(),
                    bmi = preprocessing["beratbadan"].toString(),
                    pedigree = preprocessing["pedigree"].toString(),
                    age = preprocessing["umur"].toString(),
                    label = label
                )
                predictNb(
                    pregnan = preprocessing["hamil"].toString(),
                    glucose = preprocessing["glukosa"].toString(),
                    bloodPreasure = preprocessing["tekanandarah"].toString(),
                    skin = preprocessing["ketebalankulit"].toString(),
                    insulin = preprocessing["insulin"].toString(),
                    bmi = preprocessing["beratbadan"].toString(),
                    pedigree = preprocessing["pedigree"].toString(),
                    age = preprocessing["umur"].toString(),
                    label = label
                )
            }
        }
    }

    private fun predictPreprocessing(
        predict : String
    ): Map<String,String>{
        var predictText = predict

        val pattern = Regex("(\\w+) ([+-]?([0-9]*[.])?[0-9]+)")

        val matches = pattern.findAll(predictText)
        val result = matches.map { matchResult ->
            val (word, value) = matchResult.destructured
            Pair(word, value)
        }.toMap()
        Log.d("result_patternpredict", result["hamil"].toString())
        return result
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
        label : String,
    ){
        val question = "${pregnan},"+
                "${glucose},"+
                "${bloodPressure},"+
                "${skin},"+
                "${insulin},"+
                "${bmi},"+
                "${pedigree},"+
                "${age},"

        baseContext.deleteFile("amytextfile.txt")
        val fileOutputStream: FileOutputStream = openFileOutput("amytextfile.txt", Context.MODE_PRIVATE)
        val outputWriter = OutputStreamWriter(fileOutputStream)
        outputWriter.write((question))
        outputWriter.close()
        var result = Input.main(
            this,
            "input",
            "pima/pima_train_10nr.csv",
            5
        )
        var hashresult = predictPreprocessing(result)

        var cfLabel = if (label == "0") "truenegatif" else "truepositif"
        var cfValue = if(hashresult["hasil"] != label && hashresult["hasil"] == "1")
            "falsepositif"
        else if (hashresult["hasil"] != label && hashresult["hasil"] == "0")
            "falsenegatif"
        else
            cfLabel

        insertTestingRf(
            input = question,
            cfLabel,
            cfValue,
            hashresult["hasil"].toString()
        )

        Log.d("predict_rf $cfLabel",cfValue)
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
        label : String,
    ){
        val inputData = "$pregnan $glucose $bloodPreasure $skin $insulin $bmi $pedigree $age"
        //todo 2.2 start klasifikasi nb
        var predict = classifier.predict(inputData)
        val high = predict.maxBy { it.value }.key

        var cfLabel = if (label == "0") "truenegatif" else "truepositif"
        var cfValue = if(high != label && high == "1")
            "falsepositif"
        else if (high != label && high == "0")
            "falsenegatif"
        else
            cfLabel

        insertTestingNb(
            inputData,
            cfLabel,
            cfValue,
            high
        )
    }


    private fun insertTestingRf(
        input : String,
        label : String,
        predictResult : String,
        output : String,
    ){
        repository.insertTestingRfResult(
            TestingRf(
                input,
                label,
                predictResult,
                output,
                timeCompute
            )
        )
        readAccuracy()
    }

    private fun insertTestingNb(
        input : String,
        label : String,
        predictResult : String,
        output : String,
    ){
        repository.insertTestingNvResult(
            TestingNv(
                input,
                label,
                predictResult,
                output,
                timeCompute
            )
        )
        readAccuracy()
    }

    private fun readAccuracy(){
        repository.readTestingRfResult().observe(this){
            val tp = it.count { it.predictResult == "truepositif" }
            val fp = it.count { it.predictResult == "falsepositif" }
            val tn = it.count { it.predictResult == "truenegatif" }
            val fn = it.count { it.predictResult == "falsenegatif" }

            Log.d("acc","$tp $fp $tn $fn")
            var accuracy = (tp + tn).toDouble() / (tp + tn + fp + fn).toDouble()
            var precission = (tp).toDouble() / (tp + fp).toDouble()
            var recall = (tp).toDouble() / (tp + fn).toDouble()
            var specifiy = (tn).toDouble() / (tn + tp).toDouble()
            var F1score = 2*(recall*precission / recall+precission)

            val format: NumberFormat = NumberFormat.getPercentInstance(Locale.US)

            val cfResult =
                "accuracy = ${format.format(accuracy)} \n" +
                        "precission = ${format.format(precission)} \n" +
                        "recall = ${format.format(recall)} \n" +
                        "specify = ${format.format(specifiy)} \n" +
                        "f1score = ${format.format(F1score)} \n" +
                        "tp: $tp fp: $fp tn: $tn fn: $fn"
            binding.tvPredict.text = cfResult
        }
        repository.readTestingNvResult().observe(this){
            val tp = it.count { it.predictResult == "truepositif" }
            val fp = it.count { it.predictResult == "falsepositif" }
            val tn = it.count { it.predictResult == "truenegatif" }
            val fn = it.count { it.predictResult == "falsenegatif" }

            Log.d("acc","$tp $fp $tn $fn")
            var accuracy = (tp + tn).toDouble() / (tp + tn + fp + fn).toDouble()
            var precission = (tp).toDouble() / (tp + fp).toDouble()
            var recall = (tp).toDouble() / (tp + fn).toDouble()
            var specifiy = (tn).toDouble() / (tn + tp).toDouble()
            var F1score = 2*(recall*precission / recall+precission)

            val format: NumberFormat = NumberFormat.getPercentInstance(Locale.US)

            val cfResult =
                "accuracy = ${format.format(accuracy)} \n" +
                        "precission = ${format.format(precission)} \n" +
                        "recall = ${format.format(recall)} \n" +
                        "specify = ${format.format(specifiy)} \n" +
                        "f1score = ${format.format(F1score)} \n" +
                        "tp: $tp fp: $fp tn: $tn fn: $fn"
            binding.tvResultNb.text = cfResult
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun insertDate(){
        val date = LocalDateTime.now().dayOfMonth
        val month = LocalDateTime.now().month.value
        val year = LocalDateTime.now().year

        repository.insertTodoList(
            TaskEntity(
                0,
                "test",
                2L,
                "24",
                year,
                month,
                date,
                "4",
                "2",
                false,
                false
            )
        )
        setIntervalTime( 900000,true)
    }
    private fun setIntervalTime(
        intervalTime : Long,
        activated : Boolean
    ){
        val workManager = WorkManager.getInstance(this)
        val notificationBuilder = Data.Builder()
            .putString(NOTIFICATION_Channel_ID,"TaskReminderBroadcast")
            .build()
        val periodicAlarm = PeriodicWorkRequest.Builder(
            TaskReminder::class.java,
            intervalTime,
            TimeUnit.MILLISECONDS
        ).setInputData(notificationBuilder).build()

        workManager.enqueue(periodicAlarm)
    }

    private fun insertAlgorithm(choose : Int){
        repository.insertSelectedAlgorithm(
            Algortihm(0,choose)
        )
    }
}