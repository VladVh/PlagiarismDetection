package stopwords

class Word(val text:String, val start:Int, val end:Int) {
    override fun toString(): String {
        return text
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Word) {
            text == other.text
        } else
            false
    }
}