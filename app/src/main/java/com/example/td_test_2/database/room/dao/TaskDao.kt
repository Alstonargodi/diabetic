package com.example.td_test_2.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.td_test_2.database.entity.task.TaskEntity

@Dao
abstract class TaskDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertTodoList(data : TaskEntity)

    @Query("select * from todotable where dateDay =:date and isComplete =0 order by dateDueMillis asc")
    abstract fun readTodayListTask(
        date: Int,
    ): List<TaskEntity>

    @Query("select * from todotable")
    abstract fun readTodayTask(
    ): LiveData<List<TaskEntity>>

    @Query("delete from TodoTable where title like :name ")
    abstract fun deleteTodoTask(name : String)

    @Query("delete from TodoTable")
    abstract fun deleteTask()
}