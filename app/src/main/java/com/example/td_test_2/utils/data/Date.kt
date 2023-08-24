package com.example.fts_tes.Utils

import android.util.Log
import android.util.SparseIntArray
import com.example.td_test_2.utils.data.DateMatch
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern


object Date {
    private val DATE_DD_MM_YYYY_PATTERN =
        Pattern.compile("(0?[1-9]|[12][0-9]|3[01])[- \\/.](0?[1-9]|1[012])(?:[- \\/.]((?:19|20)?\\d\\d))?")
    private val DATE_MM_DD_YYYY_PATTERN =
        Pattern.compile("(0?[1-9]|1[012])[- \\/.](0?[1-9]|[12][0-9]|3[01])(?:[- \\/.]((?:19|20)?\\d\\d))?")
    private val DATE_YYYY_MM_DD_PATTERN =
        Pattern.compile("((?:19|20)?\\d\\d)[- \\/.](0?[1-9]|1[012])[- \\/.](0?[1-9]|[12][0-9]|3[01])")

    //public static final Pattern HOURS_PATTERN = Pattern.compile("([0-9]|0[0-9]|1[0-9]|2[0-3])(?::|h)([0-5][0-9]|[1-9])? *(?i)(A.?M.?|P.?M.?)?");
    //public static final Pattern HOURS_IN_FULL_PATTERN = Pattern.compile("(?!00)(\\d\\d)( ?h| ?o'clock)");
    private val WEEK_DAY_PATTERN =
        Pattern.compile("\\b(?i)(?:mon|tue|wed|thur|fri|sat|sun|monday|tuesday|wednesday|thursday|friday|saturday|sunday|segunda|ter[cç]a|quarta|quinta|sexta|s[aá]bado|domingo|seg|ter|qua|qui|sex|s[aá]b)\\b")
    private val DATE_IN_FULL_EN_PATTERN =
        Pattern.compile("(?i)((?!00)[0-2][0-9]|[1-9]|30|31)(?:st|nd|rd|th)?(?: ?of ?| *)?(january|february|march|april|may|june|july|august|september|october|november|december|jan|feb|mar|apr|jun|jul|aug|sep|sept|oct|nov|dec)(?: *(?:,|of)? *)?((?:19|20)?\\d\\d)?")
    private val DATE_IN_FULL_MONTH_FIRST_EN_PATTERN =
        Pattern.compile("(?i)(january|february|march|april|may|june|july|august|september|october|november|december|jan|feb|mar|apr|jun|jul|aug|sep|sept|oct|nov|dec)(?: *(?:,|the)? *)?((?!00)[0-2][0-9]|[1-9]|30|31)?(?:st|nd|rd|th)?(?: *(?:,|of)? *)((?:19|20)?\\d\\d)?")
    private val DATE_IN_FULL_PT_PATTERN =
        Pattern.compile("((?!00)[0-2][0-9]|[1-9]|30|31)?(?i)(?: ?de ?| *)?\\b(janeiro|fevereiro|mar[çc]o|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro|jan|fev|mar|abr|mai|jun|jul|ago|set|out|nov|dez)\\b(?: ?de ?| *)?((?:19|20)?\\d\\d)?")
    private var MONTH_INTEGER_TO_CALENDAR: SparseIntArray? = null
    private var WEEK_DAY_TO_CALENDAR: HashMap<String, Int>? = null
    private var MONTH_TO_CALENDAR: HashMap<String, Int>? = null

    init {
        // Adding month values
        MONTH_INTEGER_TO_CALENDAR = SparseIntArray()
        MONTH_INTEGER_TO_CALENDAR!!.put(1, Calendar.JANUARY)
        MONTH_INTEGER_TO_CALENDAR!!.put(2, Calendar.FEBRUARY)
        MONTH_INTEGER_TO_CALENDAR!!.put(3, Calendar.MARCH)
        MONTH_INTEGER_TO_CALENDAR!!.put(4, Calendar.APRIL)
        MONTH_INTEGER_TO_CALENDAR!!.put(5, Calendar.MAY)
        MONTH_INTEGER_TO_CALENDAR!!.put(6, Calendar.JUNE)
        MONTH_INTEGER_TO_CALENDAR!!.put(7, Calendar.JULY)
        MONTH_INTEGER_TO_CALENDAR!!.put(8, Calendar.AUGUST)
        MONTH_INTEGER_TO_CALENDAR!!.put(9, Calendar.SEPTEMBER)
        MONTH_INTEGER_TO_CALENDAR!!.put(10, Calendar.OCTOBER)
        MONTH_INTEGER_TO_CALENDAR!!.put(11, Calendar.NOVEMBER)
        MONTH_INTEGER_TO_CALENDAR!!.put(12, Calendar.DECEMBER)

        // Adding week day values
        // mon|tue|wed|thur|fri|sat|sun|segunda|ter[cç]a|quarta|quinta|sexta|s[aá]bado|domingo
        WEEK_DAY_TO_CALENDAR = HashMap()
        WEEK_DAY_TO_CALENDAR!!["mon"] = Calendar.MONDAY
        WEEK_DAY_TO_CALENDAR!!["tue"] = Calendar.TUESDAY
        WEEK_DAY_TO_CALENDAR!!["wed"] = Calendar.WEDNESDAY
        WEEK_DAY_TO_CALENDAR!!["thur"] = Calendar.THURSDAY
        WEEK_DAY_TO_CALENDAR!!["fri"] = Calendar.FRIDAY
        WEEK_DAY_TO_CALENDAR!!["sat"] = Calendar.SATURDAY
        WEEK_DAY_TO_CALENDAR!!["sun"] = Calendar.SUNDAY
        WEEK_DAY_TO_CALENDAR!!["monday"] = Calendar.MONDAY
        WEEK_DAY_TO_CALENDAR!!["tuesday"] = Calendar.TUESDAY
        WEEK_DAY_TO_CALENDAR!!["wednesday"] = Calendar.WEDNESDAY
        WEEK_DAY_TO_CALENDAR!!["thursday"] = Calendar.THURSDAY
        WEEK_DAY_TO_CALENDAR!!["friday"] = Calendar.FRIDAY
        WEEK_DAY_TO_CALENDAR!!["saturday"] = Calendar.SATURDAY
        WEEK_DAY_TO_CALENDAR!!["sunday"] = Calendar.SUNDAY
        WEEK_DAY_TO_CALENDAR!!["segunda"] = Calendar.MONDAY
        WEEK_DAY_TO_CALENDAR!!["terca"] = Calendar.TUESDAY
        WEEK_DAY_TO_CALENDAR!!["terça"] = Calendar.TUESDAY
        WEEK_DAY_TO_CALENDAR!!["quarta"] = Calendar.WEDNESDAY
        WEEK_DAY_TO_CALENDAR!!["quinta"] = Calendar.THURSDAY
        WEEK_DAY_TO_CALENDAR!!["sexta"] = Calendar.FRIDAY
        WEEK_DAY_TO_CALENDAR!!["sabado"] = Calendar.SATURDAY
        WEEK_DAY_TO_CALENDAR!!["sábado"] = Calendar.SATURDAY
        WEEK_DAY_TO_CALENDAR!!["domingo"] = Calendar.SUNDAY
        WEEK_DAY_TO_CALENDAR!!["seg"] = Calendar.MONDAY
        WEEK_DAY_TO_CALENDAR!!["ter"] = Calendar.TUESDAY
        WEEK_DAY_TO_CALENDAR!!["qua"] = Calendar.WEDNESDAY
        WEEK_DAY_TO_CALENDAR!!["qui"] = Calendar.THURSDAY
        WEEK_DAY_TO_CALENDAR!!["sex"] = Calendar.FRIDAY
        WEEK_DAY_TO_CALENDAR!!["sab"] = Calendar.SATURDAY
        WEEK_DAY_TO_CALENDAR!!["sáb"] = Calendar.SATURDAY
        WEEK_DAY_TO_CALENDAR!!["dom"] = Calendar.SUNDAY

        // Adding month string values English and Portuguese
        // janeiro|fevereiro|mar[çc]o|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro|jan|fev|mar|abr|mai|jun|jul|ago|set|out|nov|dez
        MONTH_TO_CALENDAR = HashMap()
        MONTH_TO_CALENDAR!!["janeiro"] = Calendar.JANUARY
        MONTH_TO_CALENDAR!!["fevereiro"] = Calendar.FEBRUARY
        MONTH_TO_CALENDAR!!["marco"] = Calendar.MARCH
        MONTH_TO_CALENDAR!!["março"] = Calendar.MARCH
        MONTH_TO_CALENDAR!!["abril"] = Calendar.APRIL
        MONTH_TO_CALENDAR!!["maio"] = Calendar.MAY
        MONTH_TO_CALENDAR!!["junho"] = Calendar.JUNE
        MONTH_TO_CALENDAR!!["julho"] = Calendar.JULY
        MONTH_TO_CALENDAR!!["agosto"] = Calendar.AUGUST
        MONTH_TO_CALENDAR!!["setembro"] = Calendar.SEPTEMBER
        MONTH_TO_CALENDAR!!["outubro"] = Calendar.OCTOBER
        MONTH_TO_CALENDAR!!["novembro"] = Calendar.NOVEMBER
        MONTH_TO_CALENDAR!!["dezembro"] = Calendar.DECEMBER
        MONTH_TO_CALENDAR!!["jan"] = Calendar.JANUARY
        MONTH_TO_CALENDAR!!["fev"] = Calendar.FEBRUARY
        MONTH_TO_CALENDAR!!["mar"] = Calendar.MARCH
        MONTH_TO_CALENDAR!!["abr"] = Calendar.APRIL
        MONTH_TO_CALENDAR!!["mai"] = Calendar.MAY
        MONTH_TO_CALENDAR!!["jun"] = Calendar.JUNE
        MONTH_TO_CALENDAR!!["jul"] = Calendar.JULY
        MONTH_TO_CALENDAR!!["ago"] = Calendar.AUGUST
        MONTH_TO_CALENDAR!!["set"] = Calendar.SEPTEMBER
        MONTH_TO_CALENDAR!!["out"] = Calendar.OCTOBER
        MONTH_TO_CALENDAR!!["nov"] = Calendar.NOVEMBER
        MONTH_TO_CALENDAR!!["dez"] = Calendar.DECEMBER
        MONTH_TO_CALENDAR!!["january"] = Calendar.JANUARY
        MONTH_TO_CALENDAR!!["february"] = Calendar.FEBRUARY
        MONTH_TO_CALENDAR!!["march"] = Calendar.MARCH
        MONTH_TO_CALENDAR!!["april"] = Calendar.APRIL
        MONTH_TO_CALENDAR!!["may"] = Calendar.MAY
        MONTH_TO_CALENDAR!!["june"] = Calendar.JUNE
        MONTH_TO_CALENDAR!!["july"] = Calendar.JULY
        MONTH_TO_CALENDAR!!["august"] = Calendar.AUGUST
        MONTH_TO_CALENDAR!!["september"] = Calendar.SEPTEMBER
        MONTH_TO_CALENDAR!!["october"] = Calendar.OCTOBER
        MONTH_TO_CALENDAR!!["november"] = Calendar.NOVEMBER
        MONTH_TO_CALENDAR!!["december"] = Calendar.DECEMBER
        MONTH_TO_CALENDAR!!["feb"] = Calendar.FEBRUARY
        MONTH_TO_CALENDAR!!["apr"] = Calendar.APRIL
        MONTH_TO_CALENDAR!!["aug"] = Calendar.AUGUST
        MONTH_TO_CALENDAR!!["sep"] = Calendar.SEPTEMBER
        MONTH_TO_CALENDAR!!["sept"] = Calendar.SEPTEMBER
        MONTH_TO_CALENDAR!!["oct"] = Calendar.OCTOBER
        MONTH_TO_CALENDAR!!["dec"] = Calendar.DECEMBER
    }

    fun detectDates(query: String?): DateMatch {
        val result = DateMatch()

        // Detecting day of week
        var m = WEEK_DAY_PATTERN.matcher(query)

        if (m.find()) {
            result.day_of_week = WEEK_DAY_TO_CALENDAR!![m.group().lowercase(Locale.getDefault())]!!
            Log.d("searchtask_date_match",result.day.toString())
        }

        Log.d("searchtask_date_match",m.toString())

        // Matching DD/MM/(YY)YY
        m = DATE_DD_MM_YYYY_PATTERN.matcher(query)
        if (m.find()) {
            result.day = m.group(1)
            result.month = MONTH_INTEGER_TO_CALENDAR!![m.group(2).toInt()]
            if (m.group(3) != null) {
                result.year = m.group(3)
                if (result.year!!.length === 2) result.year = "20" + result.year
            }
            Log.d("searchtask_date_match",result.day.toString())
            return result
        }

        // Matching MM/DD/(YY)YY
        m = DATE_MM_DD_YYYY_PATTERN.matcher(query)
        if (m.find()) {
            result.month = MONTH_INTEGER_TO_CALENDAR!![m.group(1).toInt()]
            result.day = m.group(2)
            if (m.group(3) != null) {
                result.year = m.group(3)
                if (result.year!!.length === 2) result.year = "20" + result.year
            }
            Log.d("searchtask_date_match",result.day.toString())
            return result
        }

        // Matching YYYY/MM/DD
        m = DATE_YYYY_MM_DD_PATTERN.matcher(query)
        if (m.find()) {
            result.year = m.group(1)
            if (result.year!!.length === 2) result.year = "20" + result.year
            result.month = MONTH_INTEGER_TO_CALENDAR!![m.group(2).toInt()]
            result.day = m.group(3)
            Log.d("searchtask_date_match",result.day.toString())
            return result
        }
        m = DATE_IN_FULL_PT_PATTERN.matcher(query)
        if (m.find()) {
            result.day = m.group(1)
            result.month = MONTH_TO_CALENDAR!![m.group(2).lowercase(Locale.getDefault())]!!
            result.year = m.group(3)
            if (result.year != null) if (result.year!!.length === 2) result.year =
                "20" + result.year
            Log.d("searchtask_date_match",result.day.toString())
            return result
        }
        m = DATE_IN_FULL_EN_PATTERN.matcher(query)
        if (m.find()) {
            result.day = m.group(1)
            result.month = MONTH_TO_CALENDAR!![m.group(2).lowercase(Locale.getDefault())]!!
            result.year = m.group(3)
            if (result.year != null) if (result.year!!.length === 2) result.year =
                "20" + result.year
            Log.d("searchtask_date_match",result.day.toString())
            return result
        }
        m = DATE_IN_FULL_MONTH_FIRST_EN_PATTERN.matcher(query)
        if (m.find()) {
            result.day = m.group(2)
            result.month = MONTH_TO_CALENDAR!![m.group(1).lowercase(Locale.getDefault())]!!
            result.year = m.group(3)
            if (result.year != null) if (result.year!!.length === 2) result.year =
                "20" + result.year
            Log.d("searchtask_date_match",result.day.toString())
            return result
        }
        return result
    }
}