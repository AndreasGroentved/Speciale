package repositories

import datatypes.tangle.Tag
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectRepository
import org.dizitart.no2.objects.filters.ObjectFilters
import org.dizitart.no2.objects.filters.ObjectFilters.and
import org.dizitart.no2.objects.filters.ObjectFilters.eq
import java.util.*

data class Message(val text: String = "", val timestamp: Date = Date(0), val deviceID: String = "",
                   val messageChainID: String = "", val methodName: String = "none")

class MessageRepo {
    private val messageRepo: ObjectRepository<Message>

    init {
        val db = Nitrite.builder()
            .filePath("messages.db")
            .openOrCreate()
        messageRepo = db.getRepository(Message::class.java)
    }

    fun getMessages(deviceID: String): List<Message> {
        val find = messageRepo.find(ObjectFilters.eq("deviceID", deviceID))
        return find.toList()
    }

    fun getNewestMessage(deviceID: String, methodName: String, tag: Tag): Message? {
        val find = messageRepo.find(and(eq("deviceID", deviceID), eq("methodName", methodName), eq("tag", tag)))
        return find.sortedByDescending { it.timestamp }.firstOrNull()

    }

    fun saveMessage(message: Message) {
        messageRepo.insert(message)
    }
}