package helpers

import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import repositories.ReadSignatures
import java.math.BigInteger
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

object EncryptionHelper {
    private val signatures : ReadSignatures = ReadSignatures()
    private val logger: Logger = SimpleLoggerFactory().getLogger("EncryptionHelper")

    fun generateKeys(): KeyPair {
        val generator = KeyPairGenerator.getInstance("EC", "SunEC")
        val ecsp = ECGenParameterSpec("sect163k1")
        generator.initialize(ecsp)
        return generator.genKeyPair()
    }

    fun loadPrivateECKeyFromProperties(key: String): PrivateKey {
        val keyString = PropertiesLoader.instance.getProperty(key)
        val keyBytes = BigInteger(keyString).toByteArray()
        return KeyFactory.getInstance("EC", "SunEC").generatePrivate(PKCS8EncodedKeySpec(keyBytes))
    }

    fun loadPublicECKeyFromProperties(key: String): PublicKey {
        val keyString = PropertiesLoader.instance.getProperty(key)
        val keyBytes = BigInteger(keyString).toByteArray()
        return KeyFactory.getInstance("EC", "SunEC").generatePublic(X509EncodedKeySpec(keyBytes))
    }

    fun loadPublicECKeyFromBigInteger(key: BigInteger): PublicKey {
        val hest = key.toByteArray()
        return KeyFactory.getInstance("EC", "SunEC").generatePublic(X509EncodedKeySpec(hest))
    }

    fun signBase64(privateKey: PrivateKey, text: String): String {
        LogI("Signing, privateKey: $privateKey text: $text")
        val signature = Signature.getInstance("SHA1withECDSA", "SunEC")
        signature.initSign(privateKey)
        signature.update(text.toByteArray())
        val sign = signature.sign()
        return String(Base64.getEncoder().encode(sign))
    }

    fun verifySignatureBase64(publicKey: PublicKey, text: String, signatureBase64: String): Boolean {
        LogI("Verifying signature publicKey: $publicKey text: $text signatureBase64: $signatureBase64")
        val signature = Base64.getDecoder().decode(signatureBase64)
        val sig = Signature.getInstance("SHA1withECDSA", "SunEC")
        if(signatures.getAllSig().contains(signatureBase64)) {
            LogW("Already seen signature")
            return false
        }
        return try {
            sig.initVerify(publicKey)
            sig.update(text.toByteArray())
            val verified = sig.verify(signature)
            if(verified) {
                signatures.saveSig(signatureBase64)
            }
            verified
        } catch (e: Exception) {
            when (e) {
                is SignatureException -> LogW("Invalid signature")
                is InvalidKeyException -> LogW("Invalid key")
            }
            false
        }
    }
}