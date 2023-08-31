package com.example.td_test_2.presentasion.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.td_test_2.database.entity.task.TaskEntity
import com.example.td_test_2.databinding.ItemTaskBinding

class TaskHomeRecylerViewAdapter(
    private var taskItem : List<TaskEntity>
): RecyclerView.Adapter<TaskHomeRecylerViewAdapter.ViewHolder>() {

    class ViewHolder(val binding : ItemTaskBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       return ViewHolder(
           ItemTaskBinding.inflate(
               LayoutInflater.from(parent.context),
               parent,
               false
           )
       )
    }

    override fun getItemCount(): Int = taskItem.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = taskItem[position]
        holder.binding.tvTask.text = task.title
    }


}