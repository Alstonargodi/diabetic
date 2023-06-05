package com.example.fts_tes.Utils

import java.util.Locale
import java.util.Random
import java.util.StringTokenizer

class LoremIpsum(sentences: Int) {
    /**
     * Get the lines which make up the generated corpus.
     *
     * @return lines
     */
    val lines: Array<String?>
    private var str: String? = null
    private val r: Random

    /**
     * Construct a new lorem ipsum corpus consisting of the given number of
     * sentences.
     *
     * @param sentences sentences to construct
     */
    init {
        r = Random()
        lines = arrayOfNulls(sentences)
        for (i in 0 until sentences) {
            lines[i] = strFry(LINES[r.nextInt(LINES.size)])
        }
    }

    override fun toString(): String {
        if (str == null) {
            val b = StringBuilder()
            for (i in lines.indices) {
                b.append(lines[i])
                if (i + 1 < lines.size) {
                    b.append(" ")
                }
            }
            str = b.toString()
        }
        return str!!
    }

    /**
     * Randomly replace some words of the given line with randomly-selected
     * words from the static lipsum text.
     *
     * @param line
     * @return
     */
    private fun strFry(line: String): String {
        val REPLACE_RATE = 30
        val DROP_RATE = 5
        val builder = StringBuilder()
        val tokenizer = StringTokenizer(line)
        while (tokenizer.hasMoreTokens()) {
            var word = tokenizer.nextToken()
            if (r.nextInt(100) < REPLACE_RATE) {
                var newWord = randomLipsumWord()
                if (word.lowercase(Locale.getDefault()) != word) { /* need to change case of replacement */
                    newWord = newWord.substring(0, 1).uppercase(Locale.getDefault()) +
                            newWord.substring(1)
                }

                /* match ending punctuation if necessary */word = stripTrailingPunc(newWord)
            }

            /* we'll only drop lower-case words -- lazy hack to avoid
			 * killing the first word in our sentence.
			 */if (r.nextInt(100) > DROP_RATE ||
                word.lowercase(Locale.getDefault()) != word
            ) {
                builder.append(word)
                if (tokenizer.hasMoreTokens()) {
                    builder.append(" ")
                } else {
                    if (word[word.length - 1] != '.') {
                        builder.append('.')
                    }
                }
            }
        }
        return builder.toString()
    }

    private fun stripTrailingPunc(word: String): String {
        return if (word[word.length - 1] == '.' ||
            word[word.length - 1] == ','
        ) {
            word.substring(0, word.length - 1)
        } else word
    }

    private fun randomLipsumWord(): String {
        val line = LINES[r.nextInt(LINES.size)]
        val tokenizer = StringTokenizer(line)
        for (chosenToken in r.nextInt(tokenizer.countTokens()) downTo 1) {
            tokenizer.nextToken()
        }
        return stripTrailingPunc(tokenizer.nextToken().lowercase(Locale.getDefault()))
    }

    companion object {
        private val LINES = arrayOf(
            "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do " +
                    "eiusmod tempor incididunt ut labore et dolore magna " +
                    "aliqua.",
            "Ut enim ad minim veniam, quis nostrud exercitation ullamco " +
                    "laboris nisi ut aliquip ex ea commodo consequat.",
            "Duis aute irure dolor in reprehenderit in voluptate velit esse " +
                    "cillum dolore eu fugiat nulla pariatur.",
            "Excepteur sint occaecat cupidatat non proident, sunt in culpa " +
                    "qui officia deserunt mollit anim id est laborum."
        )

        fun randomCorpus(sentences: Int): String {
            return LoremIpsum(sentences).toString()
        }
    }
}