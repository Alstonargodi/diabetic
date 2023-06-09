package com.example.td_test_2.presentasion

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
import com.example.td_test_2.database.entity.WordEntity
import com.example.td_test_2.database.room.dao.WordDao
import com.example.td_test_2.database.room.json.Loadjson
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import org.json.JSONException

class ChatActivity : AppCompatActivity() {
    private val messageAdapter = GroupAdapter<GroupieViewHolder>()
    private var mAdapter: SearchResultsAdapter? = null

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
        setContentView(binding.root)

        binding.rvChat.adapter = messageAdapter
        binding.btnInsert.setOnClickListener {
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
                Log.d("chat", data.toString())
                val receive = Message(
                    setences = data.result,
                    sender = "me"
                )
                val receiveItem = ReceiveMessageItem(receive)
                messageAdapter.add(receiveItem)
            }
        })
    }

    private fun insertDataset(
        context: Context,
    ){
        val pimaArray = Loadjson.loadDiabeticJson(context)
        try {
            if (pimaArray != null){
                for (i in 0 until pimaArray.length()){
                    val item = pimaArray.getJSONObject(i)
                    WordEntity(
                        id = 0,
                        type = item.getString("type"),
                        sentence = item.getString("sentence"),
                        result = item.getString("result")
                    )
                    Log.d("populatedata",item.getString("sentence"))
                }
            }
        }catch (e : JSONException){
            Log.d("roomDb",e.message.toString())
            e.printStackTrace()
        }
    }


}