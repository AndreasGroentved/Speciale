package Tangle

import helpers.EncryptionHelper
import helpers.PropertiesLoader
import jota.IotaAPI
import jota.dto.response.SendTransferResponse
import jota.error.ArgumentException
import jota.model.Transaction
import jota.model.Transfer
import jota.utils.TrytesConverter
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.math.BigDecimal

class TangleController(
    private val logger: Logger = SimpleLoggerFactory().getLogger("TangleController")
) {

    private lateinit var deviceSpecificationTag: String
    private lateinit var nodeAddress: String
    private lateinit var nodePort: String
    private var nodeSecurity = 2
    private var nodeMinWeightMagnitude = 14
    private lateinit var iotaAPI: IotaAPI

    init {
        val loadProperties = loadProperties()
        initIotaAPI(loadProperties)
    }

    private fun loadProperties(): Boolean {
        logger.info("Loading properties")
        val properties = PropertiesLoader.instance
        deviceSpecificationTag = properties.getProperty("deviceSpecificationTag")
        nodeAddress = properties.getProperty("nodeAddress")
        nodePort = properties.getProperty("nodePort")
        nodeSecurity = properties.getProperty("nodeSecurity").toInt()
        nodeMinWeightMagnitude = properties.getProperty("nodeMinWeightMagnitude").toInt()
        return properties.getProperty("nodeDefault").toBoolean()
    }

    private fun initIotaAPI(isDefault: Boolean) {
        if (!isDefault) {
            logger.info("Building Iota API wrapper with custom properties")
            iotaAPI = IotaAPI.Builder().host(nodeAddress).port(nodePort).protocol("http").build() //whatever is in the config
        } else {
            logger.info("Building Iota API wrapper with default values")
            IotaAPI.Builder().build() //https://nodes.devnet.iota.org:443
        }

    }

    fun getTransactions(seed: String): List<Transaction> {
        logger.info("getTransactions for seed: $seed")
        val transferResponse = try {
            iotaAPI.getTransfers(seed, nodeSecurity, 0, 5, false)
        } catch (e: ArgumentException) {
            logger.error("Invalid parameters supplied for getTransactions, likely invalid seed", e)
            null
        }
        return transferResponse?.transfers?.flatMap { it.transactions } ?: listOf()
    }

    fun getMessages(seed: String, tag: String?): List<String> {
        var transactions = getTransactions(seed)
        tag.let { transactions = transactions.filter { it.tag == tag } }
        return transactions.mapNotNull { getASCIIFromTrytes(it.signatureFragments) }
    }

    fun attachTransactionToTangle(seed: String, message: String, tag: String): SendTransferResponse? {
        logger.info("Attaching transaction to tangle, seed: $seed\nmessage: $message\ntag:$tag")
        val messageTrytes = TrytesConverter.asciiToTrytes(message)
        val transfer =
            Transfer(iotaAPI.getNextAvailableAddress(seed, nodeSecurity, false).first(), 0, messageTrytes, tag)
        return try {
            iotaAPI.sendTransfer(
                seed, nodeSecurity, 9, nodeMinWeightMagnitude, listOf(transfer), null, iotaAPI.getNextAvailableAddress(seed, nodeSecurity, false).first(),
                false, false, null
            )
        } catch (e: Exception) {
            when (e) {
                is ArgumentException -> {
                    logger.error("Invalid parameters supplied for sendTransfer");null
                }
                is IllegalStateException -> {
                    logger.error("Cannot attach message to Tangle"); null
                }
                else -> throw e
            }
        }
    }

    fun attachDeviceToTangle(seed: String, tangleDeviceSpecification: String): SendTransferResponse? {
        logger.info("Attaching device to tangle, seed: $seed\ndeviceSpecification: $tangleDeviceSpecification")
        val messageTrytes = TrytesConverter.asciiToTrytes(tangleDeviceSpecification)
        val deviceSpecificationTagTrytes = TrytesConverter.asciiToTrytes(deviceSpecificationTag)
        val transfer = Transfer(iotaAPI.getNextAvailableAddress(seed, nodeSecurity, false).first(), 0, messageTrytes, deviceSpecificationTagTrytes)
        return try {
            iotaAPI.sendTransfer(
                seed, nodeSecurity, 9, nodeMinWeightMagnitude, listOf(transfer), null,
                iotaAPI.getNextAvailableAddress(seed, nodeSecurity, false).first(), false, false, null
            )
        } catch (e: Exception) {
            when (e) {
                is ArgumentException -> {
                    logger.error("Invalid parameters supplied for sendTransfer");null
                }
                is IllegalStateException -> {
                    logger.error("Cannot attach message to Tangle"); null
                }
                else -> throw e
            }
        }
    }

    fun getDevicePriceSavings(from: Long, to: Long, deviceId: String): BigDecimal {
        logger.info("getDevicePriceSavings, from: $from to: $to deviceId: $deviceId")
        //TODO registerer slukket tidsperiode, find pris i den periode
        //TODO sammenlign pris med efterfølgende periode
        return BigDecimal(12)
    }

    fun getNewestBroadcast(entityName: String, publicKey: String): Transaction? {
        logger.info("getNewestBroadcast entityName: $entityName publicKey: $publicKey")
        val transactions =
            iotaAPI.findTransactionObjectsByTag(arrayOf(entityName))
        val sorted = transactions.sortedByDescending { it.timestamp }.toList()
        return sorted.firstOrNull { transaction -> parseAndVerifyTransaction(transaction, publicKey) }
    }

    fun getNewestBroadcasts(entityName: String, publicKey: String): List<Transaction>? {
        logger.info("getNewestBroadcasts entityName: $entityName publicKey: $publicKey")
        val transactions =
            iotaAPI.findTransactionObjectsByTag(arrayOf(entityName))
        return transactions.sortedByDescending { it.timestamp }.toList().filter { transaction -> parseAndVerifyTransaction(transaction, publicKey) }
    }

    private fun parseAndVerifyTransaction(transaction: Transaction, publicKey: String): Boolean {
        val messageASCII = getASCIIFromTrytes(transaction.signatureFragments) ?: return false
        val messageTrimmed = messageASCII.trim((0).toChar())
        val signature = messageTrimmed.substringAfter("__")
        val message = messageTrimmed.substringBefore("__")
        val publicECKey = EncryptionHelper.loadPublicECKeyFromProperties(publicKey)
        return EncryptionHelper.verifySignatureBase64(publicECKey, message, signature)
    }

    private fun getASCIIFromTrytes(trytes: String): String? {
        val paddedTrytes = (trytes.length % 2).let { if (it == 1) trytes + "9" else trytes }
        return try {
            TrytesConverter.trytesToAscii(paddedTrytes)
        } catch (e: ArgumentException) {
            logger.error("Unable to convert invalid trytes")
            null
        }
    }
}