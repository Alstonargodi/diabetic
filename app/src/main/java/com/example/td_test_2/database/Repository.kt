package com.example.td_test_2.database

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ad_rf.dbconfig.diabetes.PimaEntity
import com.example.td_test_2.database.entity.algorithm.Algortihm
import com.example.td_test_2.database.entity.task.TaskEntity
import com.example.td_test_2.database.entity.testing.TestingRf
import com.example.td_test_2.database.entity.words.WordEntity
import com.example.td_test_2.database.entity.testing.TestingNv
import com.example.td_test_2.database.entity.weightresult.WeightResult
import com.example.td_test_2.database.room.DbConfig
import java.time.LocalDateTime

class Repository (
    private val db : DbConfig
){
    private val dao = db.wordDao()
    private val taskDao = db.taskDao()

    @RequiresApi(Build.VERSION_CODES.O)
    val date = LocalDateTime.now().dayOfMonth
    @RequiresApi(Build.VERSION_CODES.O)
    private val month = LocalDateTime.now().month.value

    fun insertSentence(data : WordEntity){ dao.insertSentence(data)}

    fun readSentence(): LiveData<List<WordEntity>> = dao.readSentence()

    fun insertPimaData(data : PimaEntity){dao.insertPimaData(data)  }

    fun readPimaData(): LiveData<List<PimaEntity>> = dao.readPimaData()

    fun insertTestingRfResult(data : TestingRf){ dao.insertTestingRfResult(data)}

    fun readTestingRfResult(): LiveData<List<TestingRf>> = dao.readTestingRfResult()

    fun deleteTestingRfResult(){ dao.deleteTestingRfResult()}

    fun insertTestingNvResult(data : TestingNv){ dao.insertTestingNvResult(data)}

    fun readTestingNvResult(): LiveData<List<TestingNv>> = dao.readTestingNvResult()

    fun deleteTestingNvResult(){ dao.deleteTestingNvResult()}

    fun insertTodoList(data : TaskEntity){
        taskDao.insertTodoList(data)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun readTodayTaskList(): List<TaskEntity> =
        taskDao.readTodayListTask(date)

    @RequiresApi(Build.VERSION_CODES.O)
    fun readTodayTask(): LiveData<List<TaskEntity>> =
        taskDao.readTodayTask()

    fun deleteTodoTask(name : String){
        taskDao.deleteTodoTask(name)
    }

    fun deleteTask(){
        taskDao.deleteTask()
    }

    fun readSelectedAlgorithm(): LiveData<List<Algortihm>> = dao.readSelectedAlgorithm()

    fun insertSelectedAlgorithm(data : Algortihm){
        dao.insertSelectedAlgorithm(data)
    }

    fun readWeightResult(): LiveData<List<WeightResult>> = dao.readWeightResult()
    fun sortWeightResult(
        words : String,
    ): LiveData<List<WeightResult>>{
        return dao.readWeightResult(
            words,
        )
    }

    fun readSentence(setence : String): LiveData<List<WordEntity>> =
        dao.readSentenceBy(setence)


    fun insertWeightResult(data : WeightResult){
        dao.insertWeightResult(data)
    }

    fun deleteWightResult(){
        dao.deleteWightResult()
    }
}