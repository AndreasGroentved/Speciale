package repositories

import com.google.gson.Gson
import datatypes.ClientResponse
import datatypes.Response
import datatypes.tangle.Tag
import helpers.LogE
import org.dizitart.no2.Nitrite
import org.dizitart.no2.event.ChangeInfo
import org.dizitart.no2.event.ChangeListener
import org.dizitart.no2.event.ChangedItem
import org.dizitart.no2.objects.ObjectRepository
import org.dizitart.no2.objects.filters.ObjectFilters
import org.dizitart.no2.objects.filters.ObjectFilters.and
import org.dizitart.no2.objects.filters.ObjectFilters.eq
import java.util.*

data class Message(
    val text: String = "", val timestamp: Date = Date(0), val deviceID: String = "",
    val messageChainID: String = "", val methodName: String = "none"
)

class MessageRepo {
    private val messageRepo: ObjectRepository<Message>

    init {
        val db = Nitrite.builder()
            .filePath("messages.db")
            .openOrCreate()
        messageRepo = db.getRepository(Message::class.java)
    }

    fun registerUpdates(callback: (a: Any) -> Unit) = messageRepo.register { changeInfo ->
        changeInfo.changedItems.map { changedItem ->
            val d = changedItem.document
            try {
                val m = Message(d.get("text", String::class.java).substringBefore("__"), Date(d.get("timestamp") as Long), d.get("deviceID", String::class.java), d.get("messageChainID", String::class.java), d.get("methodName", String::class.java))
                callback(m)
            } catch (e: Exception) {
                LogE(e)
            }
        }
    }


    class ListenerStuff(val id: String, val path: String) : ChangeListener {
        private val gson = Gson()
        var callback: ((a: Response) -> Unit)? = null
        override fun onChange(changeInfo: ChangeInfo) {
            changeInfo.changedItems.sortedBy { it.changeTimestamp }.reversed().filter { it.document.get("deviceID") == id }.forEach { changedItem: ChangedItem ->
                val d = changedItem.document
                try {
                    val isCorrectPath = d.get("methodName", String::class.java).toLowerCase() == path
                    if (!isCorrectPath) return@forEach
                    val m = Message(d.get("text", String::class.java).substringBefore("__"), Date(d.get("timestamp") as Long), d.get("deviceID", String::class.java), d.get("messageChainID", String::class.java), d.get("methodName", String::class.java))
                    val postMessage = gson.fromJson(m.text, ClientResponse::class.java)
                    if (!postMessage.result.toString().isEmpty()) {
                        callback?.invoke(ClientResponse(postMessage.result.toString()))
                    }
                } catch (e: Exception) {
                    LogE(e)
                }
            }
        }
    }


    fun deRegister(listener: ChangeListener) {
        messageRepo.deregister(listener)
    }

    fun registerSubscription(listener: ChangeListener) {
        messageRepo.register(listener)
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