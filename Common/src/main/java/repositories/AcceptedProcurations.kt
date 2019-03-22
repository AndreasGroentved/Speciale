package repositories

import datatypes.iotdevices.Procuration
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectRepository
import org.dizitart.no2.objects.filters.ObjectFilters
import org.dizitart.no2.objects.filters.ObjectFilters.*
import java.math.BigInteger
import java.util.*

class AcceptedProcurations {
    private val procRep: ObjectRepository<Procuration>

    init {
        val db = Nitrite.builder()
            .filePath("procuration.db")
            .openOrCreate()
        procRep = db.getRepository(Procuration::class.java)
        saveProcuration(Procuration("abc", "hest", BigInteger("165615760792845808078562255657443417455825412085455006148620929027491723303411487844979141873745096637953045819064779313008630684872882194634243766937424079829"), Date(), Date(1653256815)))
    }

    fun getProcurationsByDeviceID(deviceID: String): MutableList<Procuration> {
        val find = procRep.find(eq("deviceID", deviceID))
        return find.toList()
    }

    fun getProcurationsByRecipientPublicKey(recipientPublicKey: String): MutableList<Procuration> {
        val find = procRep.find(eq("recipientPublicKey", recipientPublicKey))
        return find.toList()
    }

    fun getProcurationsByDateInterval(dateFrom: Date, dateTo: Date): MutableList<Procuration> {
        val find = procRep.find(and(gt("dateFrom", dateFrom), lt("dateTo", dateTo)))
        return find.toList()
    }

    fun getAllProcurations(): MutableList<Procuration> {
        val find = procRep.find(ObjectFilters.ALL)
        return find.toList()
    }

    fun saveProcuration(procuration: Procuration) {
        val proc = procRep.find()
        procRep.insert(procuration)
    }
}