package com.example.td_test_2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.td_test_2.databinding.ActivityMainBinding
import com.example.td_test_2.presentasion.ChatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
       binding.btEnterData.setOnClickListener {
            val intent = Intent(this@MainActivity, EnterDataActivity::class.java)
            startActivity(intent)
        }
        binding.btSearchData.setOnClickListener {
            val intent = Intent(this@MainActivity, SearchActivity::class.java)
            startActivity(intent)
        }
        binding.btViewData.setOnClickListener {
            val intent = Intent(this@MainActivity, ViewDataActivity::class.java)
            startActivity(intent)
        }
        binding.btnSchat.setOnClickListener {
            startActivity(Intent(this@MainActivity,ChatActivity::class.java))
        }
    }
}