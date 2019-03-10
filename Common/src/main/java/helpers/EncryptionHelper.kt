package helpers

import java.math.BigInteger
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

object EncryptionHelper {

    fun generateKeys(): KeyPair {
        val generator = KeyPairGenerator.getInstance("EC", "SunEC")
        val ecsp = ECGenParameterSpec("sect163k1");
        generator.initialize(ecsp)
        return generator.genKeyPair()
    }

    fun loadPrivateECKey(key: String): PrivateKey {
        val keyString = PropertiesLoader.instance.getProperty(key)
        val hest = BigInteger(keyString).toByteArray()
        val privateKey = KeyFactory.getInstance("EC", "SunEC").generatePrivate(PKCS8EncodedKeySpec(hest))
        return privateKey
    }

    fun loadPublicECKey(key: String): PublicKey {
        val keyString = PropertiesLoader.instance.getProperty(key)
        val hest = BigInteger(keyString).toByteArray()
        return KeyFactory.getInstance("EC", "SunEC").generatePublic(X509EncodedKeySpec(hest))
    }

    fun signBase64(privateKey: PrivateKey, text: String): String {
        val signature = Signature.getInstance("SHA1withECDSA", "SunEC")
        signature.initSign(privateKey)
        signature.update(text.toByteArray())
        val sign = signature.sign()
        return String(Base64.getEncoder().encode(sign))
    }

    fun verifySignatureBase64(publicKey: PublicKey, text: String, signatureBase64: String): Boolean {
        val sig = Signature.getInstance("SHA1withECDSA", "SunEC")
        sig.initVerify(publicKey)
        sig.update(text.toByteArray())
        val signature = Base64.getDecoder().decode(signatureBase64)
        return sig.verify(signature)
    }
}