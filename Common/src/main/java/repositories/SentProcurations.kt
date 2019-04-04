package repositories

import datatypes.iotdevices.Procuration
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectRepository
import org.dizitart.no2.objects.filters.ObjectFilters
import org.dizitart.no2.objects.filters.ObjectFilters.eq
import java.util.*

class SentProcurations {
    private val procRep: ObjectRepository<Procuration>

    init {
        val db = Nitrite.builder()
            .filePath("sentProcurations.db")
            .openOrCreate()
        procRep = db.getRepository(Procuration::class.java)
    }

    fun getProcurationDeviceID(deviceID: String): Procuration? {
        val find = procRep.find(eq("deviceID", deviceID))
        println(find.map { it.messageChainID })
        return find.firstOrNull()
    }

    fun getProcurationMessageChainID(messageChainID: String): Procuration? {
        val find = procRep.find(eq("messageChainID", messageChainID))
        return find.firstOrNull()
    }

    fun saveProcuration(procuration: Procuration) {
        procRep.insert(procuration)
    }

    fun getAllSentProcurations(): MutableList<Procuration> {
        return procRep.find().toList()
    }

    fun getExpiredProcurations(): List<Procuration> {
        return procRep.find(ObjectFilters.lt("dateTo", Date())).toList()
    }
}