package com.example.td_ad.presentasion.adapter

import android.util.Log

import com.example.td_test_2.database.Message
import com.example.td_test_2.R
import com.example.td_test_2.databinding.ItemReceiveBinding
import com.example.td_test_2.databinding.ItemSendMsgBinding
import com.xwray.groupie.databinding.BindableItem

class SendMessageItem(private val message: Message) : BindableItem<ItemSendMsgBinding>() {
    override fun getLayout(): Int {
        return R.layout.item_send_msg
    }

    override fun bind(viewBinding: ItemSendMsgBinding, position: Int) {
        Log.d("message",message.toString())
        viewBinding.message = message
    }
}

class ReceiveMessageItem(private val message: Message) : BindableItem<ItemReceiveBinding>() {
    override fun getLayout(): Int {
        return R.layout.item_receive
    }

    override fun bind(viewBinding: ItemReceiveBinding, position: Int) {
        viewBinding.message = message
    }
}