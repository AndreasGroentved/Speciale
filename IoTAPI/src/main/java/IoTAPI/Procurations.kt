package IoTAPI

import datatypes.iotdevices.Procuration
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectRepository
import org.dizitart.no2.objects.filters.ObjectFilters.*
import java.util.*

class Procurations {
    private val procRep: ObjectRepository<Procuration>

    init {
        val db = Nitrite.builder()
            .filePath("procuration.db")
            .openOrCreate()
        procRep = db.getRepository(Procuration::class.java)
    }

    fun getProcurationsByDeviceID(deviceID: String): MutableList<Procuration>? {
        val find = procRep.find(eq("deviceID", deviceID))
        return find.toList()
    }

    fun getProcurationsByRecipientPublicKey(recipientPublicKey: String): MutableList<Procuration>? {
        val find = procRep.find(eq("recipientPublicKey", recipientPublicKey))
        return find.toList()
    }

    fun getProcurationsByDateInterval(dateFrom: Date, dateTo: Date): MutableList<Procuration>? {
        val find = procRep.find(and(gt("dateFrom", dateFrom), lt("dateTo", dateTo)))
        return find.toList()
    }

    fun saveProcuration(procuration: Procuration) {
        val proc = procRep.find()
        procRep.insert(procuration)
    }
}