package stopwords

class Word(val text:String, val start:Int, val end:Int) {
    override fun toString(): String {
        return text
    }
}