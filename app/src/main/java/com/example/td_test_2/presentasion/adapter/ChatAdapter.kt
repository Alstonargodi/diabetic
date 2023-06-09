package com.example.td_ad.presentasion.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.td_test_2.SearchActivity
import com.example.td_test_2.SearchResultsAdapter
import com.example.td_test_2.database.sqldb.DatabaseTable
import com.example.td_test_2.databinding.ItemChatBinding
import com.example.td_test_2.chat.tfidfmain.TfIdfHelper

class ChatAdapter(
    private val mContext : Context,
    private var dataItem : List<String>
): RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    class ViewHolder(val binding : ItemChatBinding)
        : RecyclerView.ViewHolder(binding.root)

    private var mCursor: Cursor? = null
    private var offset: IntArray? = null
    private lateinit var itemCallback : SearchResultsAdapter.OnDetailItemCallback

    fun onItemDetailCallback(callback : SearchResultsAdapter.OnDetailItemCallback){
        this.itemCallback = callback
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemChatBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return if (null == mCursor) 0 else mCursor!!.count
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataItem[0]
        holder.binding.apply {
            tvMessage.text = item.toString()
        }
        if (offset != null) {
            mCursor!!.moveToPosition(offset!![0])
        } else {
            mCursor!!.moveToPosition(position)
        }
        @SuppressLint("Range") val doctor =
            mCursor!!.getString(mCursor!!.getColumnIndex(DatabaseTable.COL_DOCTOR))
        @SuppressLint("Range") val hospital =
            mCursor!!.getString(mCursor!!.getColumnIndex(DatabaseTable.COL_HOSPITAL))
        @SuppressLint("Range") val transcript =
            mCursor!!.getString(mCursor!!.getColumnIndex(DatabaseTable.COL_TRANSCRIPT))
        @SuppressLint("Range") val date =
            mCursor!!.getString(mCursor!!.getColumnIndex(DatabaseTable.COL_DATE))
        Log.d("reply",doctor)
    }

    fun swapCursor(newCursor: Cursor?) {
        offset = null
        mCursor = newCursor
        if (mContext.javaClass == SearchActivity::class.java) {
            Log.d("SEARCHRESULTSADAPTER", "Calculating Tf x Idf")
            offset = TfIdfHelper.calcTfIdf(mContext, mCursor)
            Log.d("search_rvadapter", offset.toString())
        }
        notifyDataSetChanged()
    }

    interface OnDetailItemCallback{
        fun onDetailCallback(data : String)
    }
}