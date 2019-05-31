package IoTAPI

import helpers.PasswordHandler
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.Id
import org.dizitart.no2.objects.Index
import org.dizitart.no2.objects.Indices
import org.dizitart.no2.objects.ObjectRepository
import org.dizitart.no2.objects.filters.ObjectFilters
import java.util.*

class LoginHandler {

    private val tokenMap = mutableMapOf<String, String>()
    private val userRepo: ObjectRepository<Login>

    init {
        val db = Nitrite.builder().filePath("user.db").openOrCreate()
        userRepo = db.getRepository(Login::class.java)
        if (userRepo.find(ObjectFilters.eq("userName", "admin")).size() == 0) {
            insertUser("admin", "password")
        }
    }

    fun createToken(userName: String, password: String): String? = let {
        userRepo.find(ObjectFilters.eq("userName", userName)).firstOrNull<Login>()?.let {
            val success = PasswordHandler.verifyPassword(password, it.password, it.salt)
            if (success) {
                UUID.randomUUID().toString().apply {
                    tokenMap[this] = userName
                }
            } else ""
        }
    }

    fun validateToken(token: String?) = token?.let { tokenMap.containsKey(token) } ?: false
    fun createUser(userName: String, password: String): Boolean = insertUser(userName, password)

    private fun insertUser(userName: String, password: String): Boolean {
        val salt = PasswordHandler.generateSalt()
        val saltedPassword = PasswordHandler.hashPassword(password, salt)
        val login = Login(userName, saltedPassword!!, salt)
        return userRepo.insert(login).affectedCount > 0
    }

}

@Indices(Index(value = "userName"))
data class Login(@Id val userName: String = "", val password: String = "", val salt: String = "")