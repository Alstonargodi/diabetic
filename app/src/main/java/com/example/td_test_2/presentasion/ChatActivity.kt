package com.example.td_test_2.presentasion

import android.annotation.SuppressLint
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.td_ad.presentasion.adapter.ChatAdapter
import com.example.td_ad.presentasion.adapter.ReceiveMessageItem
import com.example.td_ad.presentasion.adapter.SendMessageItem
import com.example.td_test_2.R
import com.example.td_test_2.SearchActivity
import com.example.td_test_2.SearchResultsAdapter
import com.example.td_test_2.database.DatabaseTable
import com.example.td_test_2.database.Message
import com.example.td_test_2.databinding.ActivityChatBinding
import com.example.td_test_2.utils.TfIdfHelper
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

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
            var text = binding.etInsertChat.text.toString().trim()
            text = text.replace("[.,]","")
            searchDb(text)
            val message = Message(
                setences = text,
                sender = "me"
            )
            val sendMessageItem = SendMessageItem(message)
            messageAdapter.add(sendMessageItem)
            binding.etInsertChat.text.clear()

        }
        binding.rvChat.layoutManager = LinearLayoutManager(
            this,)
        binding.rvSearch.adapter = messageAdapter
        mAdapter = SearchResultsAdapter(this)
        binding.rvSearch.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
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
//        swapCursor(searchDb)
        mAdapter?.swapCursor(searchDb)
        mAdapter!!.onItemDetailCallback(object : SearchResultsAdapter.OnDetailItemCallback{
            override fun onDetailCallback(data: String) {
                Log.d("reply",data)
                val receive = Message(
                    setences = data,
                    sender = "me"
                )
                val receiveItem = ReceiveMessageItem(receive)
                messageAdapter.add(receiveItem)
            }
        })
    }


}