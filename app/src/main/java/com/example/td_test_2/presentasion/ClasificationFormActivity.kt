package com.example.td_test_2.presentasion

import Classifier
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.fts_tes.Utils.PerformanceTime
import com.example.td_test_2.R
import com.example.td_test_2.database.Repository
import com.example.td_test_2.database.entity.message.Message
import com.example.td_test_2.database.entity.words.WordEntity
import com.example.td_test_2.database.room.DbConfig
import com.example.td_test_2.database.sqllite.DatabaseTable
import com.example.td_test_2.databinding.ActivityChatBinding
import com.example.td_test_2.databinding.ActivityClasificationFormBinding
import com.example.td_test_2.presentasion.chatactivity.adapter.ReceiveMessageItem
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class ClasificationFormActivity : AppCompatActivity() {
    private lateinit var repository: Repository
    private val classifier = Classifier<String>()
    private lateinit var binding : ActivityClasificationFormBinding

    private var trainData = "pimall.csv"
    private var typeClassificer = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClasificationFormBinding.inflate(layoutInflater)
        repository = Repository(DbConfig.setDatabase(this))
        setContentView(binding.root)

        repository.readSelectedAlgorithm().observe(this){ algo->
            algo.forEach { typeClassificer=it.type }
        }

        binding.btnStart.setOnClickListener {
            startClasification()
        }
    }

    private fun startClasification(){
        predictRf()
    }

    private fun predictRf(){
        var pregnan = binding.etKehamilan.text.toString()
        var glucose = binding.etJumlahGlukosa.text.toString()
        var bloodPreasure = binding.etTekananDarah.text.toString()
        var skin = binding.etKetebalanKulit.text.toString()
        var insulin = binding.etInsulin.text.toString()
        var bmi = binding.etBeratBadan.text.toString()
        var pedigree = "0.627"
        var age = binding.etUmur.text.toString()

        baseContext.deleteFile("amytextfile.txt")
        val fileOutputStream: FileOutputStream = openFileOutput("amytextfile.txt", Context.MODE_PRIVATE)
        val outputWriter = OutputStreamWriter(fileOutputStream)
        outputWriter.write((
                "${pregnan},"+
                        "${glucose},"+
                        "${bloodPreasure},"+
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
        }else{
            decission = "tidak terkena diabetes"
        }
        var message = "Hasil Klasifikasi menujukan bahwa anda $decission"
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
            result = "$message",
        )

    }

    private fun replyMessageClasf(
        tipe : String,
        sentence : String,
        result : String
    ){
        var hasilkalimat = if(sentence == "Halo !") "Halo! \n selamat datang di aplikasi diabetic" else "$result\n\nwaktu komputasi ${
            PerformanceTime.TimeElapsed().toString()}"

        repository.readSentence(sentence).observe(
            this){list->
            if(list.isEmpty()){
                insertNewData(tipe, sentence, result)
            }
        }

        binding.tvHasil.text = hasilkalimat
    }

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

    private fun insertNewData(
        tipe : String,
        setence : String,
        result : String
    ){
        val db: DatabaseTable = DatabaseTable.getInstance(baseContext)!!

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