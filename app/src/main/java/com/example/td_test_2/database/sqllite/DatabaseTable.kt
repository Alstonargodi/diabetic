package com.example.td_test_2.database.sqllite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.util.Log
import com.example.fts_tes.Utils.PerformanceTime
import com.example.td_test_2.chat.tfidfmain.TfIdfMain
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DatabaseTable private constructor(context: Context) {
    private val mDatabaseOpenHelper: DatabaseOpenHelper

    companion object {
        private val TAG = "KalimatDatabase"
        private val DATABASE_NAME = "kalimat"
        private val FTS_VIRTUAL_TABLE = "SenteceTable"
        private val SINOM_TABLE = "SINONIMOS"
        private val SIGLAS_TABLE = "SIGLAS"
        private val DATABASE_VERSION = 1
        private val MY_256 = 256
        private var sDatabaseTable: DatabaseTable? = null

        // Columns for table FTS
        val COL_TIPE = "TIPE"
        val COL_PATTERN = "PATTERN"
        val COL_ANSWER = "ANSWER"
        val COL_DATE = "DATE"
        val COL_TOKENIZE = "tokenize=unicode61"
        val COL_PREFIX = "prefix=\"4\""
        val COL_MATCHINFO = "MATCHINFO"
        val COL_SNIPPET = "SNIPPET"
        val COL_OFFSETS = "OFFSETS"

        val COL_SINOM1 = "SINOM1"
        val COL_SINOM2 = "SINOM2"

        val COL_SIGLA = "SIGLA"
        val COL_SIGNIFICADO = "SIGNIFICADO"
        val MATCHINFO = "matchinfo(" + FTS_VIRTUAL_TABLE + ") as " + COL_MATCHINFO

        fun parseMatchInfoBlob(blob: ByteArray): IntArray {
            val length = blob.size
            val result = IntArray(length / 4)
            var i = 0
            while (i < length) {
                result[i / 4] = (blob[i] +
                        (blob[i + 1] * MY_256) +
                        (blob[i + 2] * MY_256 * MY_256) +
                        (blob[i + 3] * MY_256 * MY_256 * MY_256))
                i += 4
            }
            return result
        }

        fun getInstance(context: Context): DatabaseTable? {
            if (sDatabaseTable == null) {
                sDatabaseTable = DatabaseTable(context.applicationContext)
            }
            return sDatabaseTable
        }
    }

    fun addNewEntry(tipe: String, pattern: String, answer: String): Long {
        return mDatabaseOpenHelper.addEntry(tipe,pattern,answer)
    }

    init {
        mDatabaseOpenHelper = DatabaseOpenHelper(context)
    }

    class DatabaseOpenHelper internal constructor(private val mHelperContext: Context) :
        SQLiteOpenHelper(mHelperContext, DATABASE_NAME, null, DATABASE_VERSION) {
        var mDatabase: SQLiteDatabase?

        init {
            mDatabase = writableDatabase
        }

        private fun addDefaultEntries() {
            val context = mHelperContext.applicationContext
            try {
                val ins = context.resources.openRawResource(
                    context.resources.getIdentifier(
                        "base_dados",
                        "raw", context.packageName
                    )
                )
                var reader: BufferedReader? = null
                reader = BufferedReader(InputStreamReader(ins, "UTF-8"))
                while (reader.ready()) {
                    val line = reader.readLine()
                    val entries = line.split(";".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    val d = mDateFormat.parse(entries[0])
                    val cal = Calendar.getInstance()
                    cal.time = d
                    addEntryWithDate(entries[1], entries[2], entries[3], cal)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun addDefaultSinons() {
            val context = mHelperContext.applicationContext
            try {
                val ins = context.resources.openRawResource(
                    context.resources.getIdentifier(
                        "sinom_entries",
                        "raw", context.packageName
                    )
                )
                var reader: BufferedReader? = null
                reader = BufferedReader(InputStreamReader(ins, "UTF-8"))
                while (reader.ready()) {
                    val line = reader.readLine()
                    Log.d("EXEC SQL", line)
                    mDatabase!!.execSQL(line)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
            mDatabase = sqLiteDatabase
            mDatabase!!.execSQL(FTS_TABLE_CREATE)
            mDatabase!!.execSQL(SINOMIMOS_TABLE_CREATE)
            mDatabase!!.execSQL(SIGLAS_TABLE_CREATE)
            addDefaultEntries()
            addDefaultSinons()
            Log.w("DATABASE", "Database was created")
        }

        override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
            Log.w(
                TAG, "Upgrading database from version " + i + " to " +
                        i1 + ", which will destroy all old data"
            )
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE)
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SINOM_TABLE)
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SIGLAS_TABLE)
            onCreate(sqLiteDatabase)
        }

        fun addEntryWithDate(
            doctor: String?,
            hospital: String?,
            transcript: String?,
            c: Calendar
        ): Long {
            if (mDatabase == null) {
                Log.w("DATABASE", "Database is null!")
            }
            val day = c[Calendar.DAY_OF_MONTH]
            val month = c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
            val year = c[Calendar.YEAR]
            val day_of_week =
                c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())

            // String representing the current day, month, year and week day
            val date =
                "d" + String.format("%02d", day) + " m" + month + " y" + year + " w" + day_of_week
            Log.d("ENTRY DATE", date)
            val cv = ContentValues()
            cv.put(COL_TIPE, doctor)
            cv.put(COL_PATTERN, hospital)
            cv.put(COL_DATE, date)
            cv.put(COL_ANSWER, transcript)
            return mDatabase!!.insert(FTS_VIRTUAL_TABLE, null, cv)
        }

        fun addEntry(
            tipe: String?,
            pattern: String?,
            answer: String?
        ): Long {
            if (mDatabase == null) {
                Log.w("DATABASE", "Database is null!")
            }

            val c = Calendar.getInstance()
            val day = c[Calendar.DAY_OF_MONTH]
            val month = c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
            val year = c[Calendar.YEAR]
            val day_of_week =
                c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())

            val date =
                "d" + String.format("%02d", day) + " m" + month + " y" + year + " w" + day_of_week
            Log.d("CURRENT DATE", date)
            val cv = ContentValues()
            cv.put(COL_TIPE, tipe)
            cv.put(COL_PATTERN, pattern)
            cv.put(COL_DATE, date)
            cv.put(COL_ANSWER, answer)
            return mDatabase!!.insert(FTS_VIRTUAL_TABLE, null, cv)
        }

        companion object {
            private val mDateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
            private val FTS_TABLE_CREATE = "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                    " USING fts4 (" +
                    COL_TIPE + ", " +
                    COL_PATTERN + ", " +
                    COL_DATE + ", " +
                    COL_ANSWER + ", " +
                    COL_TOKENIZE + ", " +
                    COL_PREFIX + ")"
            private val SINOMIMOS_TABLE_CREATE = "CREATE TABLE " + SINOM_TABLE + " (" +
                    COL_SINOM1 + " TEXT PRIMARY KEY," +
                    COL_SINOM2 + " TEXT NOT NULL)"
            private val SIGLAS_TABLE_CREATE = "CREATE TABLE " + SIGLAS_TABLE + " (" +
                    COL_SIGLA + " TEXT PRIMARY KEY," +
                    COL_SIGNIFICADO + " TEXT NOT NULL)"
        }
    }

    //Function to search given a query string
    fun getWordMatches(
        query: String
    ): Cursor? {
        var query = query

        val selectionArgs = arrayOfNulls<String>(1)
        var terms = query.trim { it <= ' ' }
            .replace("[\\/\\-.]".toRegex(), "-").split("[- +]+".toRegex())
            .toTypedArray()

        //todo 1.3 start query berdsarkan terms
        for (term in terms) {
            Log.d("TERM", "\"" + term + "\"")
        }

        //todo 1.4 setting terms dokumen
        TfIdfMain.setSearchTerms(terms)

        var args: String = ""
        for (i in terms.indices step 1) {
            args += terms[i]
            if (i != terms.size - 1) {
                args += " OR "
            }
        }

        selectionArgs[0] = args
        val selection = FTS_VIRTUAL_TABLE + " MATCH ? COLLATE NOCASE"
        return query(selection, selectionArgs)
    }

    val allRows: Cursor
        //Function to get all data rows
        get() = mDatabaseOpenHelper.readableDatabase.rawQuery(
            "SELECT * FROM " + FTS_VIRTUAL_TABLE,
            null
        )
    val rowCount: Long
        //Function to get number of entries
        get() {
            val res =
                DatabaseUtils.queryNumEntries(mDatabaseOpenHelper.mDatabase, FTS_VIRTUAL_TABLE)
            Log.d("DATABASETABLE:", "Number of entries: $res")
            return res
        }

    fun getDocumentFrequency(word: String): Long {
        //todo 1.10 query bedasarkan terms
        val res = DatabaseUtils.queryNumEntries(
            mDatabaseOpenHelper.mDatabase,
            FTS_VIRTUAL_TABLE,
            FTS_VIRTUAL_TABLE + " match '" + word + "'"
        )
        Log.d("DATABASETABLE:", "Number of columns with $word : $res")

        return res
    }


    private fun query(
        selection: String,
        selectionArgs: Array<String?>,
    ): Cursor? {

        val builder = SQLiteQueryBuilder()
        builder.tables = FTS_VIRTUAL_TABLE
        val mColumns = arrayOf(MATCHINFO, "*")

        //todo 1.5 query dari database
        val cursor = builder.query(
            mDatabaseOpenHelper.readableDatabase,
            mColumns, selection, selectionArgs, null, null, null
        )
        Log.d("calctime_query", PerformanceTime.TimeElapsed().toString())
//        if (cursor == null) {
//            return null
//        } else if (!cursor.moveToFirst()) {
//            cursor.close()
//            return null
//        }

        return cursor
    }


}