package com.example.td_test_2.chat.preprocessing

object Tokenizer {
    private val englishStopWords = arrayOf(
        "i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours", "yourself", "yourselves",
        "he", "him", "his", "himself", "she", "her", "hers", "herself", "it", "its", "itself", "they", "them", "their",
        "theirs", "themselves", "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are", "was",
        "were", "be", "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing", "a", "an", "the",
        "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against",
        "between", "into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in",
        "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why",
        "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no",
        "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don", "should",
        "now"
    )


    fun sentenceToToken(text : String): List<String>{
        val setence = text.trim().toLowerCase()
        var tokens = setence.split(" ")
        tokens = tokens.map {
            Regex("[^A-Za-z0-9 ]").replace( it , "")
        }
        //remove stop words
//        tokens = tokens.filter {
//            !englishStopWords.contains( it.trim() )
//        }

        //slang words


        tokens = tokens.filter {
            it.trim().isNotEmpty() and it.trim().isNotBlank()
        }

        return tokens
    }

    fun removeStopWords(setence : List<String>): String {
        var words = setence.filter {
            !englishStopWords.contains( it.trim() )
        }
        return words.toString()
    }

    fun removeLineBreaks( para : String ) : String {
        return para
            .replace("\n", " " )
            .replace("\r", " " )
            .replace(" ", "" )
            .replace("?", " " )
            .toLowerCase()
    }
}