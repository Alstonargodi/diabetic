package com.example.td_test_2.presentasion.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.td_test_2.database.entity.task.TaskEntity
import com.example.td_test_2.databinding.ItemCardinformasiBinding

class InformasiRecyclerViewAdapter(
    private var taskItem : List<TaskEntity>
): RecyclerView.Adapter<InformasiRecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(val binding : ItemCardinformasiBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCardinformasiBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = taskItem.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = taskItem[position]
        holder.binding.textView24.text = task.title
    }
}