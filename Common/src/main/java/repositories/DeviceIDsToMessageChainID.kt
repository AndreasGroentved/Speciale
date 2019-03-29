package repositories

import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectRepository
import org.dizitart.no2.objects.filters.ObjectFilters

data class DeviceIDToMessageChainID(val deviceID: String = "", val messageChainID: String = "")

class DeviceIDsToMessageChainID {
    private val messageRepo: ObjectRepository<DeviceIDToMessageChainID>

    init {
        val db = Nitrite.builder()
            .filePath("deviceIDsToMessageChainID.db")
            .openOrCreate()
        messageRepo = db.getRepository(DeviceIDToMessageChainID::class.java)
    }

    fun getMessageChainID(deviceID: String): String {
        val find = messageRepo.find(ObjectFilters.eq("deviceID", deviceID))
        return find.first().messageChainID
    }

    fun savePair(deviceIDToMessageChainID: DeviceIDToMessageChainID) {
        messageRepo.insert(deviceIDToMessageChainID)
    }
}