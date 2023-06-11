package com.example.td_test_2.presentasion

import Classifier
import android.content.Context
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.td_ad.presentasion.adapter.ChatAdapter
import com.example.td_ad.presentasion.adapter.ReceiveMessageItem
import com.example.td_ad.presentasion.adapter.SendMessageItem
import com.example.td_test_2.SearchResultsAdapter
import com.example.td_test_2.database.sqldb.DatabaseTable
import com.example.td_test_2.database.entity.Message
import com.example.td_test_2.databinding.ActivityChatBinding
import com.example.td_test_2.chat.preprocessing.Tokenizer
import com.example.td_test_2.chat.preprocessing.Tokenizer.removeLineBreaks
import com.example.td_test_2.classification.data.Input
import com.example.td_test_2.database.Repository
import com.example.td_test_2.database.entity.WordEntity
import com.example.td_test_2.database.room.DbConfig
import com.example.td_test_2.database.room.dao.WordDao
import com.example.td_test_2.database.room.json.Loadjson
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import org.json.JSONException

class ChatActivity : AppCompatActivity() {
    private val messageAdapter = GroupAdapter<GroupieViewHolder>()
    private var mAdapter: SearchResultsAdapter? = null
    private lateinit var repository: Repository
    private val classifier = Classifier<String>()

    private var chatList = ArrayList<String>()
    private var reply = "empty"
    private var sizeData = 0
    private var offset: IntArray? = null
    private var mQuery = ""
    private var mCursor: Cursor? = null

    private lateinit var adapter : ChatAdapter
    private lateinit var binding : ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        repository = Repository(DbConfig.setDatabase(this))
        initializeNb()
        setContentView(binding.root)

        binding.rvChat.adapter = messageAdapter
        binding.btnInsert.setOnClickListener {
//            saya memiliki hamil 0 glukosa 340 darah 72 ketebalan 35 insulin 5 berat 536 pedigree 0687 umur 55
            var inputText = binding.etInsertChat.text.toString()

            //preprocessing
            var text = Tokenizer.sentenceToToken(inputText)
            var cleanText = Tokenizer.removeStopWords(text)
            cleanText = cleanText.replace("[.,]","")
            cleanText = removeLineBreaks(cleanText)

            //send
            searchDb(cleanText)
            val message = Message(
                setences = inputText,
                sender = "me"
            )
            val sendMessageItem = SendMessageItem(message)
            messageAdapter.add(sendMessageItem)
            binding.etInsertChat.text.clear()

        }
        binding.rvChat.layoutManager = LinearLayoutManager(
            this
        )
        binding.rvSearch.adapter = messageAdapter
        mAdapter = SearchResultsAdapter(this)
        binding.rvSearch.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.rvSearch.adapter = mAdapter
    }

    private fun searchDb(
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
                extractSentence(
                    data.type,
                    text,
                    data.result
                )
            }
        })
    }


    private fun extractSentence(
        type : String,
        question : String,
        answer : String,
    ){
        Log.d("result_type",type)
        when(type){
            "info"->{
                replyMessage(answer)
            }
            "predict"->{
                var preprocessing = predictPreprocessing(
                    question
                )
                predictNb(
                    pregnan = preprocessing["hamil"].toString(),
                    glucose = preprocessing["glukosa"].toString(),
                    bloodPreasure = preprocessing["darah"].toString(),
                    skin = preprocessing["ketebalan"].toString(),
                    insulin = preprocessing["insulin"].toString(),
                    bmi = preprocessing["berat"].toString(),
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

    private fun predictPreprocessing(
        cleanText : String
    ): Map<String,Int>{
        var predictText = cleanText
            .replace("saya","")
            .replace("memiliki","")

        val pattern = Regex("(\\w+) (\\d+)")

        val matches = pattern.findAll(predictText)
        val result = matches.map { matchResult ->
            val (word, value) = matchResult.destructured
            Pair(word, value.toInt())
        }.toMap()

        return result
    }

    private fun initializeNb(){
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
        val yes = "6 148 72 35 0 336 0.627 50"
        val no = "1 85 66 29 0 266 0351 31"
        val new = "0 340 72 35 5 536 0687 55"

        var predict = classifier.predict(inputData)
        var result = "$inputData" +
                "\n relate ${predict["1"]}" +
                "\n not relate  ${ predict["0"] }"
        Log.d("result_nb",result)
        replyMessage(result)
    }

    private fun replyMessage(
        sentence : String
    ){
        val receive = Message(
            setences = sentence,
            sender = "me"
        )
        val receiveItem = ReceiveMessageItem(receive)
        messageAdapter.add(receiveItem)
    }
}