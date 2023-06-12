package com.example.td_test_2.chat.tfidfmain

import android.content.Context
import android.database.Cursor
import android.util.Log
import com.example.td_test_2.database.sqldb.DatabaseTable
import java.util.Arrays
import java.util.TreeMap

object TfIdfHelper {
    private lateinit var searchTerms: Array<String>
    fun setSearchTerms(terms: Array<String>) {
        searchTerms = terms
    }

    /* Function to reduce the amount of information the initial parsed array
    from matchinfo returns in order to calculate the tf * idf for each row
    */
    fun shortenInitialArray(array: IntArray): IntArray {

        // Number of phrases in the query
        val phrases = array[0]

        // Number of user defined columns in the database
        val cols = array[1]

        // Creating the result array with the reduced sized
        val result = IntArray((array.size - 2) / cols + 2)

        // setting the appropriate values
        result[0] = phrases
        result[1] = cols

        // Counter for the results array;
        var counter = 2

        // Loop through the array
        for (p in 0 until phrases step 1) {
            var hits_this_row = 0
            var hits_all_rows = 0
            var docs_with_hits = 0
            for (c in 0 until cols) {
                hits_this_row += array[3 * (c + p * cols) + 2]
                hits_all_rows += array[3 * (c + p * cols) + 3]
                docs_with_hits += array[3 * (c + p * cols) + 4]
            }
            result[counter] = hits_this_row
            result[counter + 1] = hits_all_rows
            result[counter + 2] = docs_with_hits
            counter += 3
        }
        Log.d("search_tfidf",result.toString())
        return result
    }


    fun calcTfIdf(context: Context?, cursor: Cursor?): IntArray? {

        if (cursor == null) {
            Log.d("searchtask_result_tfidf", "null");
            return null
        }

        val valuesArray = ArrayList<DoubleArray>()
        val totalDocs = DatabaseTable.getInstance(context!!)!!.rowCount

        val documentFrequency = LongArray(searchTerms.size)
        val querySpaceVector = DoubleArray(searchTerms.size)

        // document frequecny terms
        for (i in searchTerms.indices) {
            documentFrequency[i] = DatabaseTable.getInstance(context)?.getDocumentFrequency(
                searchTerms[i]
            )!!


            if (documentFrequency[i] > 0) querySpaceVector[i] =
                Math.log(totalDocs.toDouble() / documentFrequency[i]) else querySpaceVector[i] = 0.0
        }

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {

            val colIndex = cursor.getColumnIndex(DatabaseTable.COL_MATCHINFO)
            val blob = cursor.getBlob(colIndex)
            val parsed: IntArray = DatabaseTable.parseMatchInfoBlob(blob)
            val shortened = shortenInitialArray(parsed)
            val phrases = shortened[0]
            val accumulator = DoubleArray(phrases)

            for (i in 0 until phrases) {

                //TODO 1 Term Frequency
                val tf = shortened[i * 3 + 2]
                Log.d("TF-IDF TF", tf.toString())


                //TODO 2 Inverted document frequency
                var idf = 0.0
                if (documentFrequency[i] > 0) idf =
                    Math.log(totalDocs.toDouble() / documentFrequency[i])
                Log.d("TF-IDF IDF", idf.toString())

                //TODO 3 TF X IDF
                val result = tf * idf
                accumulator[i] = result
            }
            valuesArray.add(accumulator)
            cursor.moveToNext()
        }
        val values = calculateVectorSpaceModel(querySpaceVector, valuesArray)

        val result = getOrderedIndexes(values)

        Log.d("TF-IDF INDEXES", Arrays.toString(result))
        Log.d("TF-IDF VALUES", values.toString())

        Log.d("searchtask_result_tfidf", result.toString());
        return result
    }

    //mencari kesaamaan antar dokumen hasil dan vector query
    private fun calculateVectorSpaceModel(
        queryVector: DoubleArray,
        documentVectors: ArrayList<DoubleArray>
    ): ArrayList<Double> {
        Log.d("value vector","calculate vector model")
        val result = ArrayList<Double>()
        val queryNorm = getVectorNorm(queryVector)
        for (i in documentVectors.indices) {
            val dotProduct = getDotProduct(queryVector, documentVectors[i])
            val docNorm = getVectorNorm(documentVectors[i])
            var value = 0.0
            if (queryNorm * docNorm > 0) value = dotProduct / (queryNorm * docNorm)
            Log.d("valuetf_scalardotproduct",value.toString())
            result.add(value)
        }
        return result
    }

    // panjang vector
    private fun getVectorNorm(vector: DoubleArray): Double {
        var result = 0.0
        for (i in vector.indices) {
            result += vector[i] * vector[i]
        }
        val srt = Math.sqrt(result)
        Log.d("valuetf_vectorlength",srt.toString())
        return srt
    }

    // dot product 2 vektor ( total pembobotan )
    private fun getDotProduct(v1: DoubleArray, v2: DoubleArray): Double {
        var result = 0.0
        for (i in v1.indices) {
            result += v1[i] * v2[i]
        }
        Log.d("valuetf_vectorweight",result.toString())
        return result
    }

    // Function to order the indexes for the results array
    private fun getOrderedIndexes(valuesArray: ArrayList<Double>): IntArray {
        val orderIndexes = IntArray(valuesArray.size)
        val map = TreeMap<Double, Int> { aDouble, t1 -> if (aDouble <= t1) -1 else 1 }
        for (i in valuesArray.indices) {
            map[valuesArray[i] + (i + 1) * 0.001] = i
        }
        var t = valuesArray.size
        for (index in map.values) {
            orderIndexes[--t] = index
        }

        return orderIndexes
    }
}