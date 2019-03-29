package repositories

import datatypes.iotdevices.Procuration
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectRepository
import org.dizitart.no2.objects.filters.ObjectFilters
import org.dizitart.no2.objects.filters.ObjectFilters.eq

class SentProcurations {
    private val procRep: ObjectRepository<Procuration>

    init {
        val db = Nitrite.builder()
            .filePath("sentProcurations.db")
            .openOrCreate()
        procRep = db.getRepository(Procuration::class.java)
    }

    fun getAllProcurations(): MutableList<Procuration> {
        val find = procRep.find(ObjectFilters.ALL)
        return find.toList()
    }

    fun getProcuration(messageChainID: String): Procuration {
        val find = procRep.find(eq("messageChainID", messageChainID))
        return find.first()
    }

    fun getProcurationDeviceID(deviceID: String): Procuration {
        val find = procRep.find(eq("deviceID", deviceID))
        return find.first()
    }

    fun saveProcuration(procuration: Procuration) {
        procRep.insert(procuration)
    }
}