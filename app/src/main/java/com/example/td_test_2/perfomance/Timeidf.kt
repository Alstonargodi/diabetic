package com.example.fts_tes.Utils

object Timeidf {
    // Start time
    private var t1: Long = 0
    private var t2: Long = 0
    private var t3: Long = 0
    private var t4: Long = 0
    private var t5: Long = 0
    private var t6: Long = 0
    private var t7: Long = 0
    private var t8: Long = 0
    private var t9: Long = 0
    private var t10: Long = 0
    private var t11: Long = 0
    private var t12: Long = 0
    private var t13: Long = 0


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

    fun setT7(t: Long) {
        t7 = t
    }

    fun setT8(t: Long) {
        t8 = t
    }

    fun setT9(t: Long) {
        t9 = t
    }

    fun setT10(t: Long) {
        t10 = t
    }

    fun setT11(t: Long) {
        t11 = t
    }

    fun setT12(t: Long) {
        t12 = t
    }

    fun setT13(t: Long) {
        t13 = t
    }

    val toastMessageNb: String
        get() {
            var res = ""
            res += """
                   preprocessing ${t1-t2}ms.
                   1.${t5} ms
                   2.${t4} ms
                   3.${t3} ms
                   4.${t2} ms
                   """.trimIndent()
            res += """
                   parsing ${t4 - t3}ms.
                   
                   """.trimIndent()
            res += """
                   regex ${t6 - t5}ms.
                   
                   """.trimIndent()
            res += """
                   readline ${t8 - t7}ms.
                   
                   """.trimIndent()
            res += """
                   regex readline ended ${t9 - t8}ms.
                   
                   """.trimIndent()
            res += """
                   FOR readline ended ${t11 - t9}ms.
                   """.trimIndent()
            res += """
                    data input readline ended ${t12 - t1}ms.
                   """.trimIndent()
            res += """
                    complete for loop readline ended ${t13 - t7}ms.
                   """.trimIndent()
            return res
        }
}