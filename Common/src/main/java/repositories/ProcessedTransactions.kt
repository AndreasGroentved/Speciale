package repositories

import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectRepository
import org.dizitart.no2.objects.filters.ObjectFilters

class ProcessedTransactions {
    private val ptHashRepo: ObjectRepository<Hash>

    init {
        val db = Nitrite.builder()
            .filePath("pt.db")
            .openOrCreate()
        ptHashRepo = db.getRepository(Hash::class.java)
    }

    fun hashStoredInDB(hash: String): Boolean {
        val find = ptHashRepo.find(ObjectFilters.eq("hash", hash))
        return find.any()
    }

    fun saveHash(hash: String) {
        ptHashRepo.insert(Hash(hash))
    }
}

private data class Hash(val hash: String)