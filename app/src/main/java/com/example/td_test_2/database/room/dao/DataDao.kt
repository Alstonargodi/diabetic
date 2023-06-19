package com.example.td_test_2.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ad_rf.dbconfig.diabetes.PimaEntity
import com.example.td_test_2.database.entity.testing.TestingRf
import com.example.td_test_2.database.entity.WordEntity
@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSentence(data : WordEntity)

    @Query("select * from kalimatTable")
    fun readSentence(): LiveData<List<WordEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPimaData(data : PimaEntity)

    @Query("select * from dataTable")
    fun readPimaData(): LiveData<List<PimaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTestingRfResult(data : TestingRf)

    @Query("select * from testingRfTable")
    fun readTestingRfResult(): LiveData<List<TestingRf>>

    @Query("delete from testingRfTable")
    fun deleteTestingRfResult()

}