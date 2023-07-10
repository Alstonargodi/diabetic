package com.example.td_test_2.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ad_rf.dbconfig.diabetes.PimaEntity
import com.example.td_test_2.database.entity.algorithm.Algortihm
import com.example.td_test_2.database.entity.testing.TestingRf
import com.example.td_test_2.database.entity.words.WordEntity
import com.example.td_test_2.database.entity.testing.TestingNv
import com.example.td_test_2.database.entity.weightresult.WeightResult

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSentence(data : WordEntity)

    @Query("select * from kalimatTable")
    fun readSentence(): LiveData<List<WordEntity>>

    @Query("select * from kalimatTable where sentence =:setence")
    fun readSentenceBy(setence : String): LiveData<List<WordEntity>>


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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTestingNvResult(data : TestingNv)

    @Query("select * from testingNvTable")
    fun readTestingNvResult(): LiveData<List<TestingNv>>

    @Query("delete from testingNvTable")
    fun deleteTestingNvResult()

    @Query("select * from algorithmTable")
    fun readSelectedAlgorithm(): LiveData<List<Algortihm>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSelectedAlgorithm(data : Algortihm)

    @Query("select * from WeightResultTable")
    fun readWeightResult(): LiveData<List<WeightResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWeightResult(data : WeightResult)

    @Query("select * from WeightResultTable")
    fun readWeight(): LiveData<List<WeightResult>>

    @Query("select * from WeightResultTable where sentence =:words")
    fun readWeightResult(words : String): LiveData<List<WeightResult>>

    @Query("delete from weightresulttable")
    fun deleteWightResult()
}