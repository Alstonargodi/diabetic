package com.example.td_test_2.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.util.Log
import com.example.fts_tes.Utils.PerformanceTime
import com.example.td_test_2.utils.TfIdfHelper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Calendar
import java.util.Locale

class DatabaseTable private constructor(context: Context) {
    private val mDatabaseOpenHelper: DatabaseOpenHelper

    companion object {
        private val TAG = "AppointmentDatabase"
        private val DATABASE_NAME = "APPOINTMENT"
        private val FTS_VIRTUAL_TABLE = "FTS"
        private val SINOM_TABLE = "SINONIMOS"
        private val SIGLAS_TABLE = "SIGLAS"
        private val DATABASE_VERSION = 1
        private val MY_256 = 256
        private var sDatabaseTable: DatabaseTable? = null

        // Columns for table FTS
        val COL_DOCTOR = "DOCTOR"
        val COL_HOSPITAL = "HOSPITAL"
        val COL_TRANSCRIPT = "TRANSCRIPT"
        val COL_DATE = "DATE"
        val COL_TOKENIZE = "tokenize=unicode61"
        val COL_PREFIX = "prefix=\"4\""
        val COL_MATCHINFO = "MATCHINFO"
        val COL_SNIPPET = "SNIPPET"
        val COL_OFFSETS = "OFFSETS"

        // Columns for table SINONIMOS
        val COL_SINOM1 = "SINOM1"
        val COL_SINOM2 = "SINOM2"

        // Columns for table SIGLAS
        val COL_SIGLA = "SIGLA"
        val COL_SIGNIFICADO = "SIGNIFICADO"
        val MATCHINFO = "matchinfo(" + FTS_VIRTUAL_TABLE + ") as " + COL_MATCHINFO
        val SNIPPET = "snippet(" + FTS_VIRTUAL_TABLE + ") as " + COL_SNIPPET
        val OFFSETS = "offsets(" + FTS_VIRTUAL_TABLE + ") as " + COL_OFFSETS
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

    // Wrapper method for add entry
    fun addNewEntry(doctor: String, hospital: String, transcript: String): Long {
        Log.d(
            "CONSULTATION", "\n\n\nHospital: "
                    + hospital + "\nDoctor: "
                    + doctor + "\nTranscript:\n\""
                    + transcript + "\""
        )
        return mDatabaseOpenHelper.addEntry(doctor, hospital, transcript)
    }

    // Wrapper method for add entry with date
    fun addNewEntryDate(
        doctor: String,
        hospital: String,
        transcript: String,
        calendar: Calendar
    ): Long {
        Log.d(
            "CONSULTATION", "\n\n\nHospital: "
                    + hospital + "\nDoctor: "
                    + doctor + "\nTranscript:\n\""
                    + transcript + "\n\""
                    + calendar.toString() + "\""
        )
        return mDatabaseOpenHelper.addEntryWithDate(doctor, hospital, transcript, calendar)
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
            cv.put(COL_DOCTOR, doctor)
            cv.put(COL_HOSPITAL, hospital)
            cv.put(COL_DATE, date)
            cv.put(COL_TRANSCRIPT, transcript)
            return mDatabase!!.insert(FTS_VIRTUAL_TABLE, null, cv)
        }

        // Function to add 1 entry to the appointment fts table
        fun addEntry(doctor: String?, hospital: String?, transcript: String?): Long {
            if (mDatabase == null) {
                Log.w("DATABASE", "Database is null!")
            }

            // Getting the current data to add in a new column
            val c = Calendar.getInstance()
            val day = c[Calendar.DAY_OF_MONTH]
            val month = c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
            val year = c[Calendar.YEAR]
            val day_of_week =
                c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())

            // String representing the current day, month, year and week day
            val date =
                "d" + String.format("%02d", day) + " m" + month + " y" + year + " w" + day_of_week
            Log.d("CURRENT DATE", date)
            val cv = ContentValues()
            cv.put(COL_DOCTOR, doctor)
            cv.put(COL_HOSPITAL, hospital)
            cv.put(COL_DATE, date)
            cv.put(COL_TRANSCRIPT, transcript)
            return mDatabase!!.insert(FTS_VIRTUAL_TABLE, null, cv)
        }

        companion object {
            private val mDateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
            private val FTS_TABLE_CREATE = "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                    " USING fts4 (" +
                    COL_DOCTOR + ", " +
                    COL_HOSPITAL + ", " +
                    COL_DATE + ", " +
                    COL_TRANSCRIPT + ", " +
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
        query: String, columns: Array<String>?,
        use4gram: Boolean, useDate: Boolean, useSynonym: Boolean
    ): Cursor? {
        /*
        * By using the table name in the match clause we are searching all
        * columns of the virtual table.
        * */
        var query = query
        Log.d("searchtask_query", query)
//        if (useDate) {
//            val dMatch: DateMatch = Date.detectDates(query)
//            val c = Calendar.getInstance()
//            Log.d("searchtask_date",dMatch.day.toString())
//            if (dMatch.day != null) {
//                PerformanceTime.setFoundDate()
//                query += " d" + dMatch.day
//            }
//            if (dMatch.month !== -1) {
//                PerformanceTime.setFoundDate()
//                c[Calendar.MONTH] = dMatch.month
//                query += " m" + c.getDisplayName(
//                    Calendar.MONTH,
//                    Calendar.SHORT,
//                    Locale.getDefault()
//                )
//            }
//            if (dMatch.year != null) {
//                PerformanceTime.setFoundDate()
//                query += " y" + dMatch.year
//            }
//            if (dMatch.day_of_week !== -1) {
//                PerformanceTime.setFoundDate()
//                c[Calendar.DAY_OF_WEEK] = dMatch.day_of_week
//                query += " w" + c.getDisplayName(
//                    Calendar.DAY_OF_WEEK,
//                    Calendar.SHORT,
//                    Locale.getDefault()
//                )
//            }
//        }

        // End date detection
        Log.d("DATE MATCHER:", query)

        PerformanceTime.setT2(Calendar.getInstance().timeInMillis)
        val selectionArgs = arrayOfNulls<String>(1)
        var terms = query.trim { it <= ' ' }
            .replace("[\\/\\-.]".toRegex(), "-").split("[- +]+".toRegex())
            .toTypedArray()

//        if (useSynonym) {
//            var array_size = terms.size
//            val terms_list = ArrayList(Arrays.asList(*terms))
//            Log.d("SEARCH TERMS B SYNONYMS", Arrays.toString(terms))
//            for (term: String? in terms) {
//                val sinom = getSinom(term)
//                Log.d("searchtask_synoms_result", sinom.toString())
//                if (sinom != null) {
//                    PerformanceTime.setFoundSinom()
//                    val temp = sinom.split(" +".toRegex()).dropLastWhile { it.isEmpty() }
//                        .toTypedArray()
//                    array_size += temp.size
//                    terms_list.addAll(Arrays.asList(*temp))
//                }
//            }
//            terms = terms_list.toTypedArray()
//            Log.d("SYNONYM", "Ended search for synonyms")
//            Log.d("SEARCH TERMS A SYNONYMS", Arrays.toString(terms))
//        }

        PerformanceTime.setT3(Calendar.getInstance().timeInMillis)

//        if (use4gram) {
//            Log.d("SEARCH", "Using 4gram!")
//            // Start 4gram
//            var array_size = terms.size
//            val temp_list = ArrayList(Arrays.asList(*terms))
//            for (term: String in terms) {
//                if (term.matches(".*\\d+.*".toRegex())) continue
//                if (term.length > 4) {
//                    val gram = term.substring(0, 4)
//                    array_size++
//                    temp_list.add("$gram*")
//                }
//            }
//            terms = temp_list.toTypedArray()
//            Log.d("SEARCH TERMS A 4Gram", Arrays.toString(terms))
//            // End 4gram
//        }
        PerformanceTime.setT4(Calendar.getInstance().timeInMillis)

        for (term in terms) {
            Log.d("TERM", "\"" + term + "\"")
        }
        // Setting the new search terms in the tfidf helper
        Log.d("Setting terms for tfidf", Arrays.toString(terms))
        TfIdfHelper.setSearchTerms(terms)
        var args: String = ""
        for (i in terms.indices step 1) {
            args += terms[i]
            if (i != terms.size - 1) {
                args += " OR "
            }
        }
        selectionArgs[0] = args
        Log.d("SELECTION ARGS", Arrays.toString(selectionArgs))

        val selection = FTS_VIRTUAL_TABLE + " MATCH ? COLLATE NOCASE"

        Log.d("searchtask_date_selection","$selection $selectionArgs $columns")
        return columns?.let { query(selection, selectionArgs, it) }
    }

    fun getSinom(expression: String?): String? {
        Log.d("searchtask_synoms_1", expression.toString())
        val result = mDatabaseOpenHelper.readableDatabase.rawQuery(
            "SELECT " + COL_SINOM2 + " FROM " +
                    SINOM_TABLE + " WHERE " + COL_SINOM1 + " = ? COLLATE NOCASE",
            arrayOf(expression)
        )
        var sinom: String? = null
        if (result != null) {
            if (result.moveToFirst()) {
                sinom = result.getString(0)
                result.close()
            }
        }
        return sinom
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
        val res = DatabaseUtils.queryNumEntries(
            mDatabaseOpenHelper.mDatabase,
            FTS_VIRTUAL_TABLE,
            FTS_VIRTUAL_TABLE + " match '" + word + "'"
        )
        Log.d("DATABASETABLE:", "Number of columns with $word : $res")

        return res
    }


    // Setting the new search terms in the tfidf helper

    //Helper function to query the database
    private fun query(
        selection: String,
        selectionArgs: Array<String?>,
        columns: Array<String>
    ): Cursor? {
        val builder = SQLiteQueryBuilder()
        builder.tables = FTS_VIRTUAL_TABLE
        val mColumns = arrayOf(MATCHINFO, "*")
        val cursor = builder.query(
            mDatabaseOpenHelper.readableDatabase,
            mColumns, selection, selectionArgs, null, null, null
        )
        if (cursor == null) {
            return null
        } else if (!cursor.moveToFirst()) {
            cursor.close()
            return null
        }
        Log.d("DATABASETABLE", cursor.columnNames.toString())
        return cursor
    }

    fun onDestroy() {
        mDatabaseOpenHelper.close()
    }

}