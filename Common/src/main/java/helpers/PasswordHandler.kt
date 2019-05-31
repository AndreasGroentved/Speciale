package helpers

import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


//based on https://dev.to/awwsmm/how-to-encrypt-a-password-in-java-42dh
object PasswordHandler {

    private const val ITERATIONS = 65536
    private const val KEY_LENGTH = 512
    private const val ALGORITHM = "PBKDF2WithHmacSHA512"
    private val secureRandom = SecureRandom()
    private const val DEFAULT_SEED_LENGTH = 16

    fun generateSalt(length: Int = DEFAULT_SEED_LENGTH): String {
        val salt = ByteArray(length)
        secureRandom.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    fun hashPassword(password: String, salt: String): String? {
        val spec = getSpec(password, salt)
        return try {
            val fac = SecretKeyFactory.getInstance(ALGORITHM)
            val securePassword = fac.generateSecret(spec).encoded
            Base64.getEncoder().encodeToString(securePassword)
        } catch (ex: NoSuchAlgorithmException) {
            LogE("no such algorithm")
            null
        } catch (ex: InvalidKeySpecException) {
            LogE("Invalid key spec")
            null
        } finally {
            spec.clearPassword()
        }
    }

    private fun getSpec(password: String, salt: String): PBEKeySpec {
        val chars = password.toCharArray()
        val bytes = salt.toByteArray()
        val spec = PBEKeySpec(chars, bytes, ITERATIONS, KEY_LENGTH)
        Arrays.fill(chars, Character.MIN_VALUE)
        return spec
    }

    fun verifyPassword(unEncryptedPassword: String, encryptedPassword: String, salt: String): Boolean =
        (hashPassword(unEncryptedPassword, salt) == encryptedPassword)

}