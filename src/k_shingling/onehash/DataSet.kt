package k_shingling.onehash

import stopwords.NGram
import java.io.ObjectOutputStream
import java.io.FileOutputStream
import java.util.ArrayList
import java.io.ObjectInputStream
import java.io.FileInputStream





object DataSet {
    var collection = LinkedHashMap<String, Map<NGram, String>>()

    fun addItem(name: String, shingling: Map<NGram, String>) {
        collection.putIfAbsent(name, shingling)
    }

    fun serialize() {
        try {
            val fos = FileOutputStream("fromPairs")
            val oos = ObjectOutputStream(fos)
            oos.writeObject(collection)
            oos.close()
            fos.close()
        } catch (ex: Exception) {
            println(ex)
        }

    }

    fun deserialize() {
        try {
            val fis = FileInputStream("fromPairs")
            val ois = ObjectInputStream(fis)
            collection = ois.readObject() as LinkedHashMap<String, Map<NGram, String>>
            ois.close()
            fis.close()
        } catch (ex: Exception) {
            println(ex)
        }

    }
}