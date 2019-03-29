package repositories

import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectRepository
import org.dizitart.no2.objects.filters.ObjectFilters
import java.util.*

data class Message(val text: String = "", val timestamp: Date = Date(0), val deviceID: String = "")

//todo: overvej om der skal gemmes messages på dine egne devices?
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

    fun saveMessage(message: Message) {
        messageRepo.insert(message)
    }
}