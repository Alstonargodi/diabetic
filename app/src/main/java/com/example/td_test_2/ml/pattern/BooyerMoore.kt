package patternmatching

//mencari index
fun BooyerMoore(
    inputText : String,
    pattern : String
): Boolean{
    val n = inputText.length
    val m = pattern.length
    val amount = IntArray(256){ -1}

    for ( i in 0 until m){
        amount[pattern[i].toInt()] = i
    }

    var skip : Int
    for (i in 0 until n-m){
        skip = 0
        for(j in m - 1 downTo 0){
            if (pattern[j] != inputText[i + j]){
                skip = Math.max(
                    1,
                    j-amount[inputText[i + j].toInt()]
                )
                break
            }
        }
        if (skip == 0) return false
    }
    return true
}

fun boyerMooreHorspoolSearch(source: String, pattern: String): Int {

    val pattChar = pattern.toCharArray()
    val patternLength: Int = pattChar.size
    if (patternLength == 0) {
        return 0
    }
    val src = source.toCharArray()
    val srcLength: Int = src.size

    var shift = IntArray(200) { patternLength }
    for (k in 0..(patternLength - 2)) {
        shift[pattChar[k].toInt()] = patternLength - 1 - k
    }

    var i = 0
    var j: Int
    while ((i + patternLength) <= srcLength) {
        j = patternLength - 1
        while (source[i + j] == pattChar[j]) {
            j -= 1
            if (j < 0) {
                return i
            }
        }
        i += shift[src[i + patternLength - 1].toInt()]
    }
    return -1
}
