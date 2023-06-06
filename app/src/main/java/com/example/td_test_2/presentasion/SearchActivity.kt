package com.example.td_test_2

import android.database.Cursor
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fts_tes.Utils.PerformanceTime.setT1
import com.example.fts_tes.Utils.PerformanceTime.setT5
import com.example.fts_tes.Utils.PerformanceTime.setT6
import com.example.fts_tes.Utils.PerformanceTime.toastMessage
import com.example.td_test_2.database.DatabaseTable
import com.example.td_test_2.databinding.ActivitySearchBinding
import com.example.td_test_2.utils.TfIdfHelper
import kotlinx.coroutines.launch
import java.util.Calendar

@Suppress("DEPRECATION")
class SearchActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySearchBinding

    private var mAdapter: SearchResultsAdapter? = null
//    private var mAsyncTask: SearchTask? = null
    private var mQuery = ""
    private var mLast4gramState = false
    private var mLastDateState = false
    private var mLastSynonymState = false
    private var mToast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Search data"
        binding.btSearchAction.setOnClickListener {
            Log.d("search_task", "click")
            var query = binding.etSearchQuery.getText().toString().trim()
            query = query.replace("[.,]","")
            mQuery = query
            mLast4gramState = binding.sw4gram.isChecked()
            mLastDateState = binding.swDate.isChecked()
            mLastSynonymState = binding.swSynonym.isChecked()
//            if (mAsyncTask != null) {
//                Log.d("search_task", "cancel")
//            }
            testDb(query)
        }
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvShowResults.layoutManager = linearLayoutManager
        mAdapter = SearchResultsAdapter(this)
        binding.rvShowResults.adapter = mAdapter
    }


    private fun testDb(
        params : String
    ){
        val cursor = DatabaseTable.getInstance(baseContext)?.getWordMatches(
            params, null,
            binding.sw4gram!!.isChecked,
            binding.swDate!!.isChecked,
            binding.swSynonym!!.isChecked
        ) // return null
        mAdapter?.swapCursor(cursor)
    }


    private fun showToast(message: String) {
        if (mToast != null) mToast!!.cancel()
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}