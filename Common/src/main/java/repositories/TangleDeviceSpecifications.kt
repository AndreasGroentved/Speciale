package repositories

import datatypes.iotdevices.TDSA
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectRepository
import org.dizitart.no2.objects.filters.ObjectFilters

class TangleDeviceSpecifications {
    private val tdsaRep: ObjectRepository<TDSA>

    init {
        val db = Nitrite.builder()
            .filePath("tdsa.db")
            .openOrCreate()
        tdsaRep = db.getRepository(TDSA::class.java)
    }

    fun getSpecs(): List<TDSA> {
        return tdsaRep.find().toList()
    }

    fun saveTDSA(tdsa: TDSA) {
        tdsaRep.insert(tdsa)

    }

    fun removeTDSA(tdsa: TDSA) {
        tdsaRep.remove(ObjectFilters.eq("tangleDeviceSpecification.deviceSpecification.id",tdsa.tangleDeviceSpecification.deviceSpecification.id))
    }
}