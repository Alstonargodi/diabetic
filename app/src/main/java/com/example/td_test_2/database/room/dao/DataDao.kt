package com.example.td_test_2.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ad_rf.dbconfig.diabetes.PimaEntity
import com.example.td_test_2.database.entity.WordEntity
@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSentence(data : WordEntity)

    @Query("select * from kalimatTable")
    fun readSentence(): LiveData<List<WordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPimaData(data : PimaEntity)

    @Query("select * from dataTable")
    fun readPimaData(): LiveData<List<PimaEntity>>
}