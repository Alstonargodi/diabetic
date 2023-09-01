package com.example.td_test_2.presentasion.fragment

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.td_test_2.R
import com.example.td_test_2.database.Repository
import com.example.td_test_2.database.entity.task.TaskEntity
import com.example.td_test_2.database.room.DbConfig
import com.example.td_test_2.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {
    private lateinit var binding : FragmentHomeBinding
    private lateinit var repository: Repository

    private lateinit var taskAdapter : TaskHomeRecylerViewAdapter
    private lateinit var infoAdapter : InformasiRecyclerViewAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater)
        repository = Repository(DbConfig.setDatabase(requireContext()))

        repository.readTodayTask().observe(viewLifecycleOwner){ taskList ->
            showTodoList(taskList)
        }
        
        return binding.root
    }

    private fun showTodoList(taskList : List<TaskEntity>){
        taskAdapter = TaskHomeRecylerViewAdapter(taskList)
        var recyclerViewTask = binding.rvTask
        recyclerViewTask.adapter = taskAdapter
        recyclerViewTask.layoutManager = LinearLayoutManager(requireContext())

        infoAdapter = InformasiRecyclerViewAdapter(taskList)
        var recviewInfo = binding.rvInformasi
        recviewInfo.adapter = infoAdapter
        recviewInfo.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,true)

    }
}