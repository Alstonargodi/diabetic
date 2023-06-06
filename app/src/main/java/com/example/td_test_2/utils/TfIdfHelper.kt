package com.example.td_test_2.utils

import android.content.Context
import android.database.Cursor
import android.util.Log
import com.example.td_test_2.database.DatabaseTable
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

    /*
    * This function takes a cursor with a search result
    * and performs the TFxIDF ranking algorithm using
    * the information provided by the matchinfo auxiliary
    * function from the module FTS3 from SQLite.
    */
    fun calcTfIdf(context: Context?, cursor: Cursor?): IntArray? {
        Log.d("searchtask_setsearchters", searchTerms[0].toString())
        Log.d("searchtask_result_tfidf", "start tfidf");

        if (cursor == null) {
            Log.d("searchtask_result_tfidf", "null");
            return null
        }
        // Array to store the tfxidf value of each row from the result
        val valuesArray = ArrayList<DoubleArray>()

        // Total number of rows in the table
        val totalDocs = DatabaseTable.getInstance(context!!)!!.rowCount

        // Document Frequency for each searched term
        val documentFrequency = LongArray(searchTerms.size)

        // Using vector space model. Vector for the query
        val querySpaceVector = DoubleArray(searchTerms.size)

        // Getting the document frequency for each terms from the database
        for (i in searchTerms.indices) {
            documentFrequency[i] = DatabaseTable.getInstance(context)?.getDocumentFrequency(
                searchTerms[i]
            )!!
            if (documentFrequency[i] > 0) querySpaceVector[i] =
                Math.log(totalDocs.toDouble() / documentFrequency[i]) else querySpaceVector[i] = 0.0
        }

        // Iterating over each result (row);
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {


            // Index of the matchinfo column in the cursor
            val colIndex = cursor.getColumnIndex(DatabaseTable.COL_MATCHINFO)
            // Retrieving information
            val blob = cursor.getBlob(colIndex)
            // Parsing the byte blob to an int array
            val parsed: IntArray = DatabaseTable.parseMatchInfoBlob(blob)
            // Collapsing information from all columns to a single row
            val shortened = shortenInitialArray(parsed)
            // Number of phrases in the query
            val phrases = shortened[0]
            // Variable to accumulate all tfxIdf values for a given row
            val accumulator = DoubleArray(phrases)

            // Go through all the phrases and calculate each tfxidf value
            for (i in 0 until phrases) {

                // Term Frequency
                val tf = shortened[i * 3 + 2]
                // Inverted document frequency
                var idf = 0.0
                if (documentFrequency[i] > 0) idf =
                    Math.log(totalDocs.toDouble() / documentFrequency[i])
                // Tf x Idf value for 1 phrase
                val result = tf * idf
                // Add value to the total of the row
                accumulator[i] = result
            }

            // Add the row value to the result array
            valuesArray.add(accumulator)
            cursor.moveToNext()
        }
        val values = calculateVectorSpaceModel(querySpaceVector, valuesArray)


        val result = getOrderedIndexes(values)

        Log.d("TF IDF INDEXES", Arrays.toString(result))
        Log.d("TF IDF VALUES", values.toString())

        Log.d("searchtask_result_tfidf", result.toString());
        return result
    }

    // Function to calculate similarity between each document result and the query vector for ordering
    private fun calculateVectorSpaceModel(
        queryVector: DoubleArray,
        documentVectors: ArrayList<DoubleArray>
    ): ArrayList<Double> {
        val result = ArrayList<Double>()
        val queryNorm = getVectorNorm(queryVector)
        for (i in documentVectors.indices) {
            val dotProduct = getDotProduct(queryVector, documentVectors[i])
            val docNorm = getVectorNorm(documentVectors[i])
            var value = 0.0
            if (queryNorm * docNorm > 0) value = dotProduct / (queryNorm * docNorm)
            result.add(value)
        }

        return result
    }

    // Helper function to get the euclidean distance from a vector
    private fun getVectorNorm(vector: DoubleArray): Double {
        var result = 0.0
        for (i in vector.indices) {
            result += vector[i] * vector[i]
        }
        return Math.sqrt(result)
    }

    // Helper function to get the dot product of 2 vectors
    private fun getDotProduct(v1: DoubleArray, v2: DoubleArray): Double {
        var result = 0.0
        for (i in v1.indices) {
            result += v1[i] * v2[i]
        }
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