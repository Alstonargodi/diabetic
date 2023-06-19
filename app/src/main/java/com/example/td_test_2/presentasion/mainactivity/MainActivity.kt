package com.example.td_test_2.presentasion.mainactivity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ad_rf.dbconfig.diabetes.PimaEntity
import com.example.td_test_2.SearchResultsAdapter
import com.example.td_test_2.chat.preprocessing.PreProcessing
import com.example.td_test_2.chat.tfidfmain.TfIdfMain
import com.example.td_test_2.database.Repository
import com.example.td_test_2.database.entity.testing.TestingRf
import com.example.td_test_2.database.room.DbConfig
import com.example.td_test_2.database.room.json.Loadjson
import com.example.td_test_2.database.sqllite.DatabaseTable
import com.example.td_test_2.databinding.ActivityMainBinding
import com.example.td_test_2.presentasion.chatactivity.ChatActivity
import com.example.td_test_2.presentasion.enterdataactivity.EnterDataActivity
import com.example.td_test_2.presentasion.mainactivity.adapter.TestingResultAdapter
import com.example.td_test_2.presentasion.searchactivity.SearchActivity
import com.example.td_test_2.presentasion.viewdataactivity.ViewDataActivity
import com.example.td_test_2.utils.UtilsSetences
import org.json.JSONException
import randomforest.Input
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.NumberFormat
import java.util.Locale

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        repository = Repository(DbConfig.setDatabase(this))

        repository.readPimaData().observe(this){
            Log.d("data",it.toString())
        }

        setContentView(binding.root)
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
                repository.deleteTestingRfResult()
                readCsv()
            }
            btnSchat.setOnClickListener {
                startActivity(Intent(this@MainActivity, ChatActivity::class.java))
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
        testList.add(text)
        for(i in 0 until testList.size){
            mAdapter = SearchResultsAdapter(this)
            val searchDb = DatabaseTable.getInstance(baseContext)?.getWordMatches(
                testList[i],
                null,
                true,
                true,
                true
            )
            swapCursor(searchDb)
            if (offset != null) {
                mCursor!!.moveToPosition(offset!![i])
            } else {
                mCursor!!.moveToPosition(i)
            }
            @SuppressLint("Range") val type = mCursor!!.getString(mCursor!!.getColumnIndex(DatabaseTable.COL_TIPE))
            @SuppressLint("Range") val response = mCursor!!.getString(mCursor!!.getColumnIndex(DatabaseTable.COL_ANSWER))
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
        Log.d("SEARCHRESULTSADAPTER", "Calculating Tf x Idf")
        //todo 1.7 proses perhitungan TFIDF
        offset = TfIdfMain.calcTfIdf(this, mCursor)
        timeCompute = Input.timeCompute

    }

    private fun showTestResult(
        result: List<String>,
    ){
        trainingAdapter = TestingResultAdapter(result)
        binding.rvTest.layoutManager = LinearLayoutManager(this)
        binding.rvTest.adapter = trainingAdapter
    }

    fun testingRf(){
            var data = PimaEntity(
                1,
                "6",
                "148",
                "72",
                "35",
                "0",
                "33.6",
                "0.627",
                "50",
                ""
            )

        baseContext.deleteFile("amytextfile.txt")
        val fileOutputStream: FileOutputStream = openFileOutput("amytextfile.txt", Context.MODE_PRIVATE)
        val outputWriter = OutputStreamWriter(fileOutputStream)
        outputWriter.write((
                "${data.pregnan},"+
                        "${data.glucose},"+
                        "${data.bloodPressure},"+
                        "${data.skinThich},"+
                        "${data.insulin},"+
                        "${data.bmi},"+
                        "${data.pedigree},"+
                        "${data.age},"
                )
        )
        outputWriter.close()
        var result = Input.main(this,"test",10)
        Log.d("testingInput",result)
    }


    private fun readCsv(){
        val dataInput = UtilsSetences.csvToStringI(
            this,
            "testing/pimakalimat_test_10.csv"
        )
        Log.d("testdata", dataInput.toString())
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
                null,
                true,
                true,
                true
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
            Log.d("AA${type}",setence.toString())
//            Log.d("AA${type}", if(type == typeTest) "true" else "false")
//            Log.d("AA${type}", if(response == responseTest) "true" else "false")
//            var result = "Q:${setence[i]} " +
//                    "\ntypecorrect: ${if(type == typeTest) "true" else "false"} " +
//                    "\nresponsecorrect: ${if(response == responseTest) "true" else "false"} " +
//                    "\nreseponse" +
//                    "\n$responseTest"
//            resultList.add(result)
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
        var result = Input.main(this,"input",15)
        var hashresult = predictPreprocessing(result)

        var cfLabel = if (label == "0") "truenegatif" else "truepositif"
        var cfValue = if(hashresult["hasil"] != label && hashresult["hasil"] == "1")
            "falsepositif"
        else if (hashresult["hasil"] != label && hashresult["hasil"] == "0")
            "falsenegatif"
        else
            cfLabel

        insertTestingResult(
            input = question,
            cfLabel,
            cfValue,
            hashresult["hasil"].toString()
        )

        Log.d("predict_rf $cfLabel",cfValue)

    }

    private fun insertTestingResult(
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
                        "f1score = ${format.format(F1score)}" +
                        "waktu komputasi = $timeCompute"
            binding.tvPredict.text = cfResult
        }
    }
}