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