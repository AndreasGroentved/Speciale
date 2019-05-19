package IoTAPI

import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.Id
import org.dizitart.no2.objects.Index
import org.dizitart.no2.objects.Indices
import org.dizitart.no2.objects.ObjectRepository
import java.util.*

class LoginHandler {

    private val tokenMap = mutableMapOf<String, String>()
    private val userRepo: ObjectRepository<Login>

    init {
        val db = Nitrite.builder()
            .filePath("user.db")
            .openOrCreate()
        userRepo = db.getRepository(Login::class.java)
        userRepo.find().firstOrNull() ?: userRepo.insert(Login("admin", "password"))
    }

    fun createToken(login: Login): String? = login.let {
        if (userRepo.find().firstOrNull<Login>() != login) return null
        val uuid = UUID.randomUUID().toString()
        tokenMap[uuid] = login.userName
        uuid
    }

    fun validateToken(token: String) = tokenMap.containsKey(token)

    fun createUser(login: Login) = userRepo.insert(login).affectedCount > 0

}

@Indices(Index(value = "userName"))
data class Login(@Id val userName: String = "", val password: String = "")