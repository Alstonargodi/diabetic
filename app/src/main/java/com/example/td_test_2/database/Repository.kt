package com.example.td_test_2.database

import androidx.lifecycle.LiveData
import com.example.ad_rf.dbconfig.diabetes.PimaEntity
import com.example.td_test_2.database.entity.testing.TestingRf
import com.example.td_test_2.database.entity.WordEntity
import com.example.td_test_2.database.entity.testing.TestingNv
import com.example.td_test_2.database.room.DbConfig

class Repository (
    private val db : DbConfig
){
    private val dao = db.wordDao()

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


}