package com.example.fts_tes.Utils

object PerformanceTime {
    // Start time
    private var t1: Long = 0

    // Time after date detection
    private var t2: Long = 0

    // Time after 4 gram
    private var t3: Long = 0

    // Time after Synonyms;
    private var t4: Long = 0

    // Time after sql
    private var t5: Long = 0

    // Time after Tf idf
    private var t6: Long = 0
    private var foundDate = false
    private var foundSinom = false
    fun setFoundDate() {
        foundDate = true
    }

    fun setFoundSinom() {
        foundSinom = true
    }

    fun setT1(t: Long) {
        t1 = t
    }

    fun setT2(t: Long) {
        t2 = t
    }

    fun setT3(t: Long) {
        t3 = t
    }

    fun setT4(t: Long) {
        t4 = t
    }

    fun setT5(t: Long) {
        t5 = t
    }

    fun setT6(t: Long) {
        t6 = t
    }

    val toastMessage: String
        get() {
            var res = ""
            if (foundDate) {
                res += "Date found!\n"
                foundDate = false
            }
            if (foundSinom) {
                res += "Synonym found!\n"
                foundSinom = false
            }
            res += """
                   Date Detection ended ${t2 - t1}ms.
                   
                   """.trimIndent()
            res += """
                   Synonym ended ${t3 - t1}ms.
                   
                   """.trimIndent()
            res += """
                   4gram conversion ended ${t4 - t1}ms.
                   
                   """.trimIndent()
            res += """
                   Sql ended ${t5 - t1}ms.
                   
                   """.trimIndent()
            res += """
                   Tf idf ended ${t6 - t1}ms.
                   
                   """.trimIndent()
            return res
        }
}