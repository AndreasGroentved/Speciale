package repositories

import datatypes.iotdevices.Procuration
import datatypes.iotdevices.ProcurationAck
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectRepository
import org.dizitart.no2.objects.filters.ObjectFilters

class ProcurationAcks {
    private val procRep: ObjectRepository<ProcurationAck>

    init {
        val db = Nitrite.builder()
            .filePath("ProcurationAcks.db")
            .openOrCreate()
        procRep = db.getRepository(ProcurationAck::class.java)
    }

    fun getAllProAck(): List<ProcurationAck> {
        return procRep.find().toList()
    }

    fun getAllAcceptedProAck(): List<ProcurationAck> {
        return procRep.find(ObjectFilters.eq("accept",true)).toList()
    }

    fun saveProAck(procurationAck: ProcurationAck) {
        procRep.insert(procurationAck)
    }

    fun removeProAcks(procurations: List<Procuration>) {
        procurations.forEach {
            procRep.remove(ObjectFilters.eq("messageChainID", it.messageChainID))
        }
    }
}