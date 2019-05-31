package IoTAPI

import helpers.PasswordHandler
import org.junit.Assert
import org.junit.Test

class PasswordHandlerTest {


    @Test
    fun testVerifyPassword() {
        val unencrypted = "IOTA"
        val salt = PasswordHandler.generateSalt()
        val encryptedPass = PasswordHandler.hashPassword(unencrypted, salt)
        Assert.assertTrue(PasswordHandler.verifyPassword(unencrypted, encryptedPass!!, salt))
        Assert.assertFalse(PasswordHandler.verifyPassword(unencrypted + "what", encryptedPass, salt))
    }

}