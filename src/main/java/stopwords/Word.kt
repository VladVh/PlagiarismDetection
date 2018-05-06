package stopwords

import java.io.Serializable

class Word(var text:String = "", val start:Int = -1, val end:Int = -1, val tag:String = ""): Serializable {

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