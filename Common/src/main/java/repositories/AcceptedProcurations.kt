package repositories

import datatypes.iotdevices.Procuration
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectRepository
import org.dizitart.no2.objects.filters.ObjectFilters
import java.util.*

class AcceptedProcurations {
    private val procRep: ObjectRepository<Procuration>

    init {
        val db = Nitrite.builder()
            .filePath("procuration.db")
            .openOrCreate()
        procRep = db.getRepository(Procuration::class.java)
    }

    fun getAcceptedProcurations(): List<Procuration> {
        val find = procRep.find(ObjectFilters.and(ObjectFilters.lt("dateFrom", Date()), ObjectFilters.gt("dateTo", Date())))
        return find.toList()
    }

    fun getAllProcurations(): List<Procuration> {
        return procRep.find(ObjectFilters.ALL).toList()
    }

    fun getProcuration(messageChainID: String): Procuration? {
        val find = procRep.find(ObjectFilters.and(ObjectFilters.eq("messageChainID", messageChainID), ObjectFilters.gt("dateTo", Date())))
        return find.firstOrNull()
    }

    fun getExpiredProcurations(): List<Procuration> {
        return procRep.find(ObjectFilters.lt("dateTo", Date())).toList()
    }

    fun getExpiredProcurationsLessThan7DaysOld(): List<Procuration> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -7)
        return procRep.find(ObjectFilters.and(
            ObjectFilters.lt("dateTo", Date()), ObjectFilters.gt("dateTo",calendar.time))).toList()
    }

    fun saveProcuration(procuration: Procuration) {
        procRep.insert(procuration)
    }
}