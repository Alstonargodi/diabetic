package com.example.td_test_2.presentasion.enterdataactivity

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.td_test_2.chat.preprocessing.PreProcessing
import com.example.td_test_2.database.Repository
import com.example.td_test_2.database.entity.words.WordEntity
import com.example.td_test_2.database.room.DbConfig
import com.example.td_test_2.database.room.json.Loadjson
import com.example.td_test_2.database.sqllite.DatabaseTable
import com.example.td_test_2.databinding.ActivityEnterDataBinding
import org.json.JSONException

class EnterDataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEnterDataBinding
    private lateinit var repository: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterDataBinding.inflate(layoutInflater)
        repository = Repository(DbConfig.setDatabase(this))
        setContentView(binding.root)
        DatabaseTable.getInstance(this)
        title = "Enter Data"

        binding.btSubmitData.setOnClickListener {
            insertDataset(this)
        }

    }

    private inner class AddFakeDataTask :
        AsyncTask<Void?, Void?, Void?>() {
        override fun onPostExecute(aVoid: Void?) {
            addToast("Entries added")
            clearInputs()
        }

        override fun doInBackground(vararg params: Void?): Void? {
            addInfo()
            return null
        }
    }

    private inner class AddDataTask :
        AsyncTask<Void?, Void?, Long>() {

        override fun onPostExecute(result: Long) {
            val idString = result.toString()
            addToast("New row with id: $idString")
            clearInputs()
        }

        override fun doInBackground(vararg params: Void?): Long {
            return storeInfo()
        }
    }

    private fun insertDataset(
        context: Context,
    ){
        val db: DatabaseTable = DatabaseTable.getInstance(baseContext)!!
        val setenceArray = Loadjson.loadSentenceJson(context)
        try {
            if (setenceArray != null){
                for (i in 0 until setenceArray.length()){
                    val item = setenceArray.getJSONObject(i)

                    var inputCsv = item.getString("kalimat")
                    var cleanText = PreProcessing.preprocessingKalimat(
                        this,
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
        }catch (e : JSONException){
            Log.d("roomDb",e.message.toString())
            e.printStackTrace()
        }
    }

    private fun addInfo() {
        val i = 0
        val db: DatabaseTable = DatabaseTable.getInstance(baseContext)!!
    }

    private fun storeInfo(): Long {
        val hospital = binding.etHospitalName.text.toString()
        val doctor = binding.etDoctorName.text.toString()
        val transcript = binding.etTranscript.text.toString()
        return if (hospital == "" || doctor == "" || transcript == "") -1 else DatabaseTable.getInstance(
            baseContext
        )?.addNewEntry(doctor, hospital, transcript)!!
    }


    private fun clearInputs() {
        binding.etDoctorName.text.clear()
        binding.etDoctorName.text.clear()
        binding.etTranscript.text.clear()
        binding.etTranscript.clearFocus()
    }

    private fun addToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}