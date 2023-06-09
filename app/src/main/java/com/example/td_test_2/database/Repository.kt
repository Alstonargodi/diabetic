package com.example.td_test_2.database

import androidx.lifecycle.LiveData
import com.example.ad_rf.dbconfig.diabetes.PimaEntity
import com.example.td_test_2.database.room.DbConfig

class Repository (
    private val db : DbConfig
){
    private val dao = db.wordDao()

    fun insertPimaData(data : PimaEntity){dao.insertPimaData(data)  }

    fun readPimaData(): LiveData<List<PimaEntity>> = dao.readPimaData()
}