package com.example.td_test_2.presentasion.viewdataactivity
import android.database.Cursor
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.td_test_2.SearchResultsAdapter
import com.example.td_test_2.database.sqllite.DatabaseTable
import com.example.td_test_2.databinding.ActivityViewDataBinding

class ViewDataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewDataBinding

    private var mAdapter: SearchResultsAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvListResults.setLayoutManager(linearLayoutManager)
        binding.rvListResults.setHasFixedSize(true)
        mAdapter = SearchResultsAdapter(this)
        binding.rvListResults.adapter = mAdapter
        SearchTask().execute()
        Log.d("view show","view data")
    }

    private inner class SearchTask :
        AsyncTask<Void?, Void?, Cursor?>() {
        override fun onPostExecute(cursor: Cursor?) {
            if (cursor == null) {
                Log.d("VIEW DATA", "Cursor is null")
                return
            }
            Log.d("VIEW COUNT", "Search ended with " + cursor.count + " results")
            if (cursor.count <= 0) {
                return
            }
            mAdapter?.swapCursor(cursor)
        }

        override fun doInBackground(vararg params: Void?): Cursor? {
            return DatabaseTable.getInstance(baseContext)?.allRows
        }
    }
}