package com.example.td_test_2

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.td_test_2.R
import com.example.td_test_2.database.DatabaseTable
import com.example.td_test_2.presentasion.ChatActivity
import com.example.td_test_2.utils.TfIdfHelper

class SearchResultsAdapter(private val mContext: Context) :
    RecyclerView.Adapter<SearchResultsAdapter.ResultViewHolder>() {
    private var mCursor: Cursor? = null
    private var offset: IntArray? = null
    private lateinit var itemCallback : OnDetailItemCallback

    fun onItemDetailCallback(callback : OnDetailItemCallback){
        this.itemCallback = callback
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ResultViewHolder {
        val context = parent.context
        val layoutID: Int = R.layout.search_result_item
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(layoutID, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        if (offset != null) {
            mCursor!!.moveToPosition(offset!![position])
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
        var pos: String? = ""
        if (offset != null) pos = Integer.toString(position + 0)
        holder.doctorName.text = doctor
        holder.hospitalName.text = hospital
        holder.displayTranscript.text = transcript
        holder.displayDate.text = date
        holder.displayIndex.text = pos
        if (mContext.javaClass == ChatActivity::class.java) {
            itemCallback.onDetailCallback(transcript)
        }
    }

    override fun getItemCount(): Int {
        return if (null == mCursor) 0 else mCursor!!.count
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

    inner class ResultViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var doctorName: TextView
        var hospitalName: TextView
        var displayTranscript: TextView
        var displayDate: TextView
        var displayIndex: TextView

        init {
            doctorName = itemView.findViewById(R.id.tv_display_doctor_name)
            hospitalName = itemView.findViewById(R.id.tv_display_hospital_name)
            displayTranscript = itemView.findViewById(R.id.tv_display_transcript)
            displayDate = itemView.findViewById(R.id.tv_display_date)
            displayIndex = itemView.findViewById(R.id.tv_display_result_index)
        }
    }

    interface OnDetailItemCallback{
        fun onDetailCallback(data : String)
    }
}