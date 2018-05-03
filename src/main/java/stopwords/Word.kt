package stopwords

class Word(var text:String = "", val start:Int = -1, val end:Int = -1, val tag:String = "") {

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