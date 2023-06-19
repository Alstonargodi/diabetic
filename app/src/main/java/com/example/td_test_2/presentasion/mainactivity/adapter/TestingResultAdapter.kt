package com.example.td_test_2.presentasion.mainactivity.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.td_test_2.databinding.ItemTestBinding

class TestingResultAdapter(
    private var result : List<String>,
): RecyclerView.Adapter<TestingResultAdapter.ViewHolder>() {
    class ViewHolder(val binding : ItemTestBinding)
        : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemTestBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = result.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val it = result[position]
        holder.binding.tvTestingresult.text = it
    }
}