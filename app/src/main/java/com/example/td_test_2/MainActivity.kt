package com.example.td_test_2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ad_rf.dbconfig.diabetes.PimaEntity
import com.example.td_test_2.chat.preprocessing.PreProcessing
import com.example.td_test_2.chat.preprocessing.Utils
import com.example.td_test_2.database.Repository
import com.example.td_test_2.database.entity.WordEntity
import com.example.td_test_2.database.room.DbConfig
import com.example.td_test_2.database.room.json.Loadjson
import com.example.td_test_2.database.sqldb.DatabaseTable
import com.example.td_test_2.databinding.ActivityMainBinding
import com.example.td_test_2.databinding.ActivitySearchBinding
import com.example.td_test_2.presentasion.ChatActivity
import org.json.JSONException
import org.xml.sax.Parser
import randomforest.Input
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: Repository

    private var mAdapter: SearchResultsAdapter? = null
    //    private var mAsyncTask: SearchTask? = null
    private var testList = MutableLiveData<List<String>>()

    private var mQuery = ""
    private var mToast: Toast? = null
    private var cleanText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        repository = Repository(DbConfig.setDatabase(this))

        repository.readPimaData().observe(this){
            Log.d("data",it.toString())
        }
        test()

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
//                runtest()
                testingRf()
            }
            btnSchat.setOnClickListener {
                startActivity(Intent(this@MainActivity,ChatActivity::class.java))
            }
        }
        mAdapter = SearchResultsAdapter(this)
        binding.rvTest.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.rvTest.adapter = mAdapter
    }


    fun test(){
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
                    testList.value = data
                }
            }
        }catch (exception: IOException) {
            exception.printStackTrace()
        } catch (exception: JSONException) {
            exception.printStackTrace()
        }
    }

    fun runtest(){
        repository.readSentence().observe(this){
            it.forEach {
                if (it.type != "reminder"){
                    queryDatabase(it.sentence)
                }
            }
        }
    }
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
//                setenceSelect(
//                    data.type,
//                    text,
//                    data.result
//                )
            }
        })
    }

    private fun setenceSelect(
        type : String,
        question : String,
        answer : String,
    ){
        Log.d("chat", type)
//        when(type){
//            "info"->{
//                Log.d("chat_info", answer)
//            }
//            "predict"->{
//                //preprocessing prediksi dari kalimat input
//                Log.d("chat_predict", answer)
//            }
//            "reminder"->{
//                Log.d("chat_reminder", answer)
//            }
//            "help"->{
//                Log.d("chat_help", answer)
//            }
//        }
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
}