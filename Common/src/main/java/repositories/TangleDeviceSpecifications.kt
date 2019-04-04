package repositories

import datatypes.iotdevices.TDSA
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectRepository
import org.dizitart.no2.objects.filters.ObjectFilters
import org.dizitart.no2.objects.filters.ObjectFilters.eq

class TangleDeviceSpecifications {
    private val tdsaRep: ObjectRepository<TDSA>

    init {
        val db = Nitrite.builder()
            .filePath("tdsa.db")
            .openOrCreate()
        tdsaRep = db.getRepository(TDSA::class.java)
    }

    fun getAllSpecs(): List<TDSA> {
        return tdsaRep.find().toList()
    }

    fun getAllPermissionedSpecs(permissionedIDs: List<String>): List<TDSA> {
        return tdsaRep.find().toList().filter { permissionedIDs.contains(it.tangleDeviceSpecification.deviceSpecification.id) }
    }

    fun getUnpermissionedSpec(unpermissionedID: String): TDSA? {
        return tdsaRep.find(eq("tangleDeviceSpecification.deviceSpecification.id", unpermissionedID)).firstOrNull()
    }

    fun saveTDSA(tdsa: TDSA) {
        tdsaRep.insert(tdsa)
    }

    fun removeTDSA(tdsa: TDSA) {
        tdsaRep.remove(ObjectFilters.eq("tangleDeviceSpecification.deviceSpecification.id",tdsa.tangleDeviceSpecification.deviceSpecification.id))
    }
}