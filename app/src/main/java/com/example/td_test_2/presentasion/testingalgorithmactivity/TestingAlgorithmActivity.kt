package com.example.td_test_2.presentasion.testingalgorithmactivity

import Classifier
import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.td_test_2.SearchResultsAdapter
import com.example.td_test_2.chat.tfidfmain.TfIdfMain
import com.example.td_test_2.database.Repository
import com.example.td_test_2.database.entity.testing.TestingNv
import com.example.td_test_2.database.entity.testing.TestingRf
import com.example.td_test_2.database.room.DbConfig
import com.example.td_test_2.database.sqllite.DatabaseTable
import com.example.td_test_2.databinding.ActivityTestingAlgorithmBinding
import com.example.td_test_2.utils.ConfussionMatrix
import com.example.td_test_2.utils.UtilsSetences
import randomforest.Input
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.NumberFormat
import java.util.Locale

class TestingAlgorithmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestingAlgorithmBinding
    private lateinit var repository: Repository
    private var mAdapter: SearchResultsAdapter? = null
    private var resultList = arrayListOf<String>()
    private var mCursor: Cursor? = null
    private var offset: IntArray? = null
    private var timeCompute = ""
    private var classifier = Classifier<String>()

    private var tree = 0
    private var trainData = "pima/pima_train_10nr.csv"
    private var testData = "testing/pimakalimat_test_10.csv"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestingAlgorithmBinding.inflate(layoutInflater)
        repository = Repository(DbConfig.setDatabase(this))
        setContentView(binding.root)

        binding.btnStartUji.setOnClickListener {
            binding.pgbarAlgoritma.visibility = View.VISIBLE
            if(binding.etTreeInsert.text.toString().isEmpty()){
                tree = 5
            }else{
                tree = binding.etTreeInsert.text.toString().toInt()
            }
            testClassification()
        }
    }

    private fun testClassification(){
        val dataInput = UtilsSetences.csvToStringI(
            this,
            testData
        )
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

        dataInput.forEach {
            readDatabase(it.features,it.label)
        }
    }


    private fun swapCursor(newCursor: Cursor?) {
        offset = null
        mCursor = newCursor

        //todo 1.7 proses perhitungan TFIDF
        offset = TfIdfMain.calcTfIdf(this, mCursor)
        timeCompute = Input.timeCompute

    }

    private fun readDatabase(
        setence: List<String>,
        label : String,
    ){
        var position = 0
        for(element in setence){
            mAdapter = SearchResultsAdapter(this)
            val searchDb = DatabaseTable.getInstance(baseContext)?.getWordMatches(
                element,
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
            if (type == "predict"){
                var preprocessing =  predictPreprocessing(setence.toString())
                predictRf(
                    pregnancies = preprocessing["hamil"].toString(),
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
        return result
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
        label : String,
    ){
        val question = "${pregnancies},"+
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
            trainData,
            tree
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

            binding.tvresultUjirf.text = ConfussionMatrix.calculateConfussionMatrix(tp, fp, tn, fn)
        }
        repository.readTestingNvResult().observe(this){
            val tp = it.count { it.predictResult == "truepositif" }
            val fp = it.count { it.predictResult == "falsepositif" }
            val tn = it.count { it.predictResult == "truenegatif" }
            val fn = it.count { it.predictResult == "falsenegatif" }

            binding.tvresultUjinb.text = ConfussionMatrix.calculateConfussionMatrix(tp, fp, tn, fn)
        }
        binding.pgbarAlgoritma.visibility = View.GONE
    }
}