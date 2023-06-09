import android.util.Log
import com.example.td_test_2.classification.data.Input
import kotlin.math.ln

class Classifier<data : Any> {
    private var inputs : MutableList<Input<data>> = mutableListOf()

    //meghitung prioritas
    private val logPrior : Map<data,Double> by lazy {
        inputs.map {
            it.category
        }.groupingBy { it }.eachCount().mapValues { value ->
            ln(value.value / inputs.size.toDouble())
        }
    }

    //menghitung jumlah kalimat
    private val allWordsCount by lazy { inputs.asSequence().flatMap {
        it.features.asSequence()
    }.distinct().count() }

    //menghitugn jumlah fitur
    private val featureCounter by lazy {
        val feat = mutableMapOf<data,Int>()
        for (a in inputs)
            feat.merge(
                a.category,
                a.features.size,
                Int::plus
            )
        Log.d("feat",feat.toString())
        feat
    }


    //memasukan data train ke dalam list inputs
    fun train(input : Input<data>){
        inputs.add(input)
        Log.d("datatrain",inputs[0].toString())
    }

    fun train(inputs : MutableList<Input<data>>){
        this.inputs = inputs
    }

    //TODO 2.2 PREDIKSI DATA BARU
    fun predict(
        input : String
    ): Map<data,Double>{
        val mapPredict = mutableMapOf<String,Map<data,Int>>()
        for (w in input.split("").distinct().toList()){
            val categoryCounter = mutableMapOf<data,Int>()
            for(i in inputs){
                if(w in i.features){
                    categoryCounter.merge(
                        i.category,
                        1,
                        Int::plus
                    )
                }else{
                    categoryCounter.merge(
                        i.category,
                        0,
                        Int::plus
                    )
                }
                mapPredict[w] = categoryCounter
            }
        }

        val resultMap = mutableMapOf<data,Double>()
        for(key in mapPredict){
            for ((cat,count) in key.value){
                val math = ln((count.toDouble() + 1.0) / (featureCounter.getOrDefault(cat,0).toDouble() + allWordsCount.toDouble()))
                resultMap.merge(cat,math,Double::plus)
            }
        }

        val maps = mutableListOf<Map<data,Double>>()
        maps.add(logPrior)
        maps.add(resultMap)
        return normalize(sumMaps(maps))
    }

    private fun sumMaps(
            maps : List<Map<data,Double>>
    ): Map<data,Double>{
        val sum = mutableMapOf<data,Double>()
        for (map in maps){
            for((key,value) in map){
                val current = sum.getOrDefault(key,0.0)
                sum[key] = current + value
            }
        }
        return sum
    }

    private fun normalize(
        suggestions : Map<data,Double>
    ): Map<data,Double>{
        Log.d("normalise",suggestions.toString())
        val max : Double = suggestions.maxBy {
            it.value
        }.value
        val values = suggestions.mapValues {
            Math.exp(it.value - max)
        }
        val norm = values.values.sum()
        return values.mapValues {
            it.value / norm
        }
    }

}