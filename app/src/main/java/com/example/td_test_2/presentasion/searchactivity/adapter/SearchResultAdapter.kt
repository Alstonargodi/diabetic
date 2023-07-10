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
import com.example.fts_tes.Utils.PerformanceTime
import com.example.td_test_2.database.sqllite.DatabaseTable
import com.example.td_test_2.presentasion.chatactivity.ChatActivity
import com.example.td_test_2.chat.tfidfmain.TfIdfMain
import com.example.td_test_2.database.entity.words.WordEntity
import com.example.td_test_2.presentasion.mainactivity.MainActivity
import com.example.td_test_2.presentasion.searchactivity.SearchActivity
import java.io.FileOutputStream
import java.io.OutputStreamWriter

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
        @SuppressLint("Range") val type =
            mCursor!!.getString(mCursor!!.getColumnIndex(DatabaseTable.COL_TIPE))
        @SuppressLint("Range") val pattern =
            mCursor!!.getString(mCursor!!.getColumnIndex(DatabaseTable.COL_PATTERN))
        @SuppressLint("Range") val response =
            mCursor!!.getString(mCursor!!.getColumnIndex(DatabaseTable.COL_ANSWER))

        var pos: String? = ""
        if (offset != null) pos = Integer.toString(position + 0)
        holder.doctorName.text = type
        holder.hospitalName.text = pattern
        holder.displayTranscript.text = response
        holder.displayIndex.text = pos

        Log.d("chat", type)
        val fileOutputStream: FileOutputStream = mContext.openFileOutput("testingidf.txt", Context.MODE_PRIVATE)
        val outputWriter = OutputStreamWriter(fileOutputStream)
        outputWriter.write(("$type"))
        outputWriter.close()
        //todo 1.16 mengembalikan hasil query berupa kesamaan dokumen
        if (mContext.javaClass == ChatActivity::class.java || mContext.javaClass == SearchActivity::class.java || mContext.javaClass == MainActivity::class.java) {
            itemCallback.onDetailCallback(
                WordEntity(
                    0,
                    type,
                    pattern,
                    response
                )
            )

        }
    }

    override fun getItemCount(): Int {
        return if (null == mCursor) 0 else 5
    }

    fun swapCursor(newCursor: Cursor?) {
        offset = null
        mCursor = newCursor
        if (mContext.javaClass == ChatActivity::class.java ||mContext.javaClass == SearchActivity::class.java || mContext.javaClass == MainActivity::class.java){
            Log.d("SEARCHRESULTSADAPTER", "Calculating Tf x Idf")
            //todo 1.7 proses perhitungan TFIDF
            offset = TfIdfMain.calcTfIdf(mContext, mCursor)
            Log.d("calctime_tfidfend", PerformanceTime.TimeElapsed().toString())
        }
        notifyDataSetChanged()
    }

    inner class ResultViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var doctorName: TextView
        var hospitalName: TextView
        var displayTranscript: TextView
        var displayIndex: TextView

        init {
            doctorName = itemView.findViewById(R.id.tv_display_doctor_name)
            hospitalName = itemView.findViewById(R.id.tv_display_hospital_name)
            displayTranscript = itemView.findViewById(R.id.tv_display_transcript)
            displayIndex = itemView.findViewById(R.id.tv_display_result_index)
        }
    }

    interface OnDetailItemCallback{
        fun onDetailCallback(data : WordEntity)


    }
}