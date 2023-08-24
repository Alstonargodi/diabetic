import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException


class NaiveBayes(useLap: Boolean) {
    private var useLaplace = false
    private var numTrainingExamples = 0
    private var numFeatures = 0
    private lateinit var featureMatrix: Array<IntArray>
    private lateinit var LabelVector: IntArray
    private var posCount = 0
    private var negCount = 0
    private lateinit var featureCountsPos: IntArray
    private lateinit var featureCountsNeg: IntArray

    init {
        useLaplace = useLap
    }

    private fun getClassification(featureVector: IntArray): Int {
        var posDenom = posCount.toDouble()
        var negDenom = negCount.toDouble()
        if (useLaplace) {
            posDenom += featureCountsPos.size.toDouble()
            negDenom += featureCountsNeg.size.toDouble()
        }
        var logProbPos = Math.log(posCount.toDouble() / (posCount + negCount))
        var logProbNeg = Math.log(negCount.toDouble() / (posCount + negCount))
        var posClass = 0.0
        var negClass = 0.0
        for (i in featureVector.indices) {
            if (featureVector[i] == 1) {
                // has a "1" in position i and is of class pos or neg
                posClass = featureCountsPos[i].toDouble()
                negClass = featureCountsNeg[i].toDouble()
            } else {
                // has a "0" in position i and is of class positive or negative
                posClass = (posCount - featureCountsPos[i]).toDouble()
                negClass = (negCount - featureCountsNeg[i]).toDouble()
            }
            if (useLaplace) {
                posClass += 1.0
                negClass += 1.0
            }
            logProbPos += Math.log(posClass / posDenom)
            logProbNeg += Math.log(negClass / negDenom)
        }
        return if (logProbPos > logProbNeg) 1 else 0
    }

    fun testNaiveBayes() {
        var numNeg = 0
        var numPos = 0
        var numCorrectNeg = 0
        var numCorrectPos = 0
        for (i in featureMatrix.indices) {
            val classification = getClassification(featureMatrix[i])
            if (LabelVector[i] == 0) {
                numNeg++
                if (classification == LabelVector[i]) numCorrectNeg++
            } else {
                numPos++
                if (classification == LabelVector[i]) numCorrectPos++
            }
        }
        println("Class 0: tested $numNeg, correctly classified $numCorrectNeg")
        println("Class 1: tested $numPos, correctly classified $numCorrectPos")
        println("Overall: tested " + (numNeg + numPos) + ", correctly classified " + (numCorrectPos + numCorrectNeg))
        println("Accuracy = " + (numCorrectPos + numCorrectNeg).toDouble() / (numNeg + numPos))
    }

    /*
	 * Trains the naive bayes classification model by initializing an array
	 * for positive class counts and one for negative class counts.  It then
	 * iterates over the data and adds up the number of places where we see a
	 * 1 with Y = 1 and the number of places we see a 1 with Y = 0.
	 */
    fun trainNaiveBayes() {
        featureCountsPos = IntArray(numFeatures)
        featureCountsNeg = IntArray(numFeatures)
        for (i in featureMatrix.indices) {
            //Calculate the num of positive instance or negative instance
            if (LabelVector[i] == 1) posCount++ else negCount++
            for (j in featureMatrix[0].indices) {
                if (LabelVector[i] == 1) featureCountsPos[j] += featureMatrix[i][j] else featureCountsNeg[j] += featureMatrix[i][j]
            }
        }
    }

    /*
	 * Input file are always of the format
	 *    <number of features>
	 *    <number of training examples>
	 *    < ... data ... >
	 *    <feature data> : <label>
	 * This method reads those constants and sets up the appropriate instance variables.
	 */
    @Throws(NumberFormatException::class, IOException::class)
    private fun readFileConstants(input: BufferedReader) {
        // Get num features and num training examples
        numFeatures = input.readLine().toInt()
        numTrainingExamples = input.readLine().toInt()
        featureMatrix = Array(numTrainingExamples) { IntArray(numFeatures) }
        LabelVector = IntArray(numTrainingExamples)
    }

    /*
	 * Reads in the feature data and ground truth vector from
	 * given input file.
	 */
    fun readFeatureData(fname: String?) {
        try {
            val input = BufferedReader(FileReader(fname))
            readFileConstants(input)
            var lineVector: Array<String>
            var i = 0
            var line = input.readLine()
            while (line != null) {
                lineVector = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (j in 0 until lineVector.size - 1) {
                    // semi-colon denotes the end of the feature data
                    if (lineVector[j].indexOf(':') != -1) {
                        lineVector[j] = lineVector[j].substring(0, 1)
                    }
                    featureMatrix[i][j] = lineVector[j].toInt()
                }
                //The last position of line is "Label"
                LabelVector[i] = lineVector[lineVector.size - 1].toInt()
                i++
                line = input.readLine()
            }
            input.close()
        } catch (e: IOException) {
            e.printStackTrace()
            System.exit(1)
        }
    }

    
}