package stopwords

class Passage(val isFirst: Boolean) {
    private var nGrams = ArrayList<Pair<NGram, NGram>>()

    fun addPair(pair: Pair<NGram, NGram>) {
        nGrams.add(pair)
    }

    fun findDistanceToNGram(newPair: Pair<NGram, NGram>): Int {
//        var distance = 0
//        var minDistance = Int.MAX_VALUE
//        for (pair in nGrams) {
//            distance = pair.first.countDistanceTo(newPair.first)
//            if (distance < minDistance)
//                minDistance = distance
//        }
        return when {
            nGrams.size == 0 -> {
                0
            }
            isFirst -> nGrams.map { it -> it.first.countDistanceTo(newPair.first) }.min()!!

            else -> nGrams.map { it -> it.second.countDistanceTo(newPair.second) }.min()!!
        }
    }
    fun getPairs() = nGrams.clone() as ArrayList<Pair<NGram, NGram>>

    fun getPassageStart(isFirst: Boolean): Int {
        nGrams.sortBy { it -> if (isFirst) it.first.start else it.second.start }
        return if (isFirst)
            nGrams[0].first.start
        else
            nGrams[0].second.start
    }

    fun getPassageEnd(isFirst: Boolean): Int {
        nGrams.sortBy { it -> if (isFirst) it.first.end else it.second.end }
        return if (isFirst)
            nGrams.last().first.end
        else
            nGrams.last().second.end
    }
}