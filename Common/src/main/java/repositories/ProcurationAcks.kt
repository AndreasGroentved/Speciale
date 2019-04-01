package repositories

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

    //todo use this
    fun getProAck(): ProcurationAck {
        val find = procRep.find(ObjectFilters.ALL)
        return find.first()
    }

    fun saveProcuration(procurationAck: ProcurationAck) {
        procRep.insert(procurationAck)
    }
}