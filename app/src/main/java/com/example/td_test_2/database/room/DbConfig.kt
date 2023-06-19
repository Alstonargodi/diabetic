package com.example.td_test_2.database.room

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ad_rf.dbconfig.diabetes.PimaEntity
import com.example.td_test_2.database.entity.testing.TestingRf
import com.example.td_test_2.database.entity.WordEntity
import com.example.td_test_2.database.entity.testing.TestingDf
import com.example.td_test_2.database.entity.testing.TestingNv
import com.example.td_test_2.database.room.dao.WordDao
import com.example.td_test_2.database.room.json.Loadjson
import org.json.JSONException
import java.util.concurrent.Executors

@Database(
    entities = [
        WordEntity::class,
        PimaEntity::class,
        TestingRf::class,
        TestingDf::class,
        TestingNv::class
    ],
    version = 3,
    exportSchema = false
)

abstract class DbConfig : RoomDatabase(){
    abstract fun wordDao(): WordDao

    companion object {
        @Volatile
        private var INSTANCE: DbConfig? = null

        @JvmStatic
        fun setDatabase(context: Context): DbConfig {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DbConfig::class.java, "sentenceDb"
                ).addCallback(object : Callback(){
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        //TODO 3
                        Executors.newSingleThreadExecutor().execute {
                            insertDataset(
                                context,
                                setDatabase(context).wordDao(),
                            )
                            insertPimaDataset(
                                context,
                                setDatabase(context).wordDao(),
                            )
                        }
                    }
                })
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private fun insertDataset(
            context: Context,
            dataDao: WordDao
        ){
            val setence = Loadjson.loadSentenceJson(context)

            try {
                if (setence != null){
                    for (i in 0 until setence.length()){
                        val item = setence.getJSONObject(i)
                        dataDao.insertSentence(
                            WordEntity(
                                id = 0,
                                type = item.getString("type"),
                                sentence = item.getString("sentence"),
                                result = item.getString("result")
                            )
                        )
                    }
                }
            }catch (e : JSONException){
                Log.d("roomDb",e.toString())
                e.printStackTrace()
            }
        }

        private fun insertPimaDataset(
            context: Context,
            dataDao: WordDao
        ){
            val pima = Loadjson.loadPimaJson(context)

            try {
                if (pima != null){
                    for (i in 0 until pima.length()){
                        val item = pima.getJSONObject(i)
                        dataDao.insertPimaData(
                            PimaEntity(
                                id = 0,
                                pregnan = item.getString("Pregnancies"),
                                glucose = item.getString("Glucose"),
                                bloodPressure = item.getString("BloodPressure") ,
                                skinThich = item.getString("SkinThickness"),
                                insulin = item.getString("Insulin"),
                                bmi = item.getString("BMI"),
                                pedigree =item.getString("DiabetesPedigreeFunction") ,
                                age = item.getString("Age"),
                                outcome = item.getString("Outcome"),
                            )
                        )
                    }
                }
            }catch (e : JSONException){
                Log.d("roomDb",e.toString())
                e.printStackTrace()
            }
        }
    }
}
