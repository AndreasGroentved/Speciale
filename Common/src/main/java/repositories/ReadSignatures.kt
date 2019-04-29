package repositories

import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectRepository

data class Signature(val sig: String = "")

class ReadSignatures {
    private val sigRep: ObjectRepository<Signature>

    init {
        val db = Nitrite.builder()
            .filePath("ReadSignatures.db")
            .openOrCreate()
        sigRep = db.getRepository(Signature::class.java)
    }

    fun getAllSig(): List<String> {
        return sigRep.find().toList().mapNotNull { it.sig }
    }

    fun saveSig(sig: String) {
        sigRep.insert(Signature(sig))
    }
}