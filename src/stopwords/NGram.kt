package stopwords

import java.io.Serializable
import kotlin.math.abs
import kotlin.math.max

class NGram(private val value:String, val start:Int, val end:Int): Serializable {
    override fun equals(other: Any?): Boolean {
        other?.apply {
            if (other::class.java.isAssignableFrom(NGram::class.java))
                return value == (other as NGram).value
        }
        return false
    }

    override fun toString(): String {
        return value
    }

    fun countVeryFrequent(veryFrequent: ArrayList<String>): Int {
        val words = value.split(" ")
        return words.count { it in veryFrequent }
    }

    fun countVeryFrequentSequence(veryFrequent: ArrayList<String>): Int {
        val words = value.split(" ")
        var count = 0
        var maxCount = 0
        for (word in words)
            if (word in veryFrequent)
                count++
            else {
                if (maxCount < count)
                    maxCount = count
                count = 0
            }
        if (maxCount < count)
            maxCount = count
        return maxCount
    }

    fun countDistanceTo(other: NGram): Int {
        return when {
            start > other.end -> start - other.end
            other.start > end -> other.start - end
            else -> 0//max(abs(start - other.start), abs(end - other.end))
        }
    }

}