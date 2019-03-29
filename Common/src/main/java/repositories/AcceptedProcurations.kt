package repositories

import datatypes.iotdevices.Procuration
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectRepository
import org.dizitart.no2.objects.filters.ObjectFilters

class AcceptedProcurations {
    private val procRep: ObjectRepository<Procuration>

    init {
        val db = Nitrite.builder()
            .filePath("procuration.db")
            .openOrCreate()
        procRep = db.getRepository(Procuration::class.java)
    }

    fun getAllProcurations(): MutableList<Procuration> {
        val find = procRep.find(ObjectFilters.ALL)
        return find.toList()
    }

    //TODO: fjern expired n shit
    fun getProcuration(messageChainID: String): Procuration? {
        val find = procRep.find(ObjectFilters.eq("messageChainID",messageChainID))
        return find.firstOrNull()
    }

    fun saveProcuration(procuration: Procuration) {
        procRep.insert(procuration)
    }
}