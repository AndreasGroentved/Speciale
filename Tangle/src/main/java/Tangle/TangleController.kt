package Tangle

import com.google.gson.Gson
import datatypes.iotdevices.PostMessage
import datatypes.iotdevices.PostMessageHack
import datatypes.iotdevices.Procuration
import datatypes.tangle.Tag
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
import repositories.ProcessedTransactions
import java.math.BigDecimal
import java.math.BigInteger

class TangleController(private val logger: Logger = SimpleLoggerFactory().getLogger("TangleController")) {

    private lateinit var nodeAddress: String
    private lateinit var nodePort: String
    private var nodeSecurity = 2
    private var nodeMinWeightMagnitude = 14
    private lateinit var iotaAPI: IotaAPI
    private val gson = Gson()
    private val pt = ProcessedTransactions()

    init {
        initIotaAPI(loadProperties())
    }

    private fun loadProperties(): Boolean {
        logger.info("Loading properties")
        val properties = PropertiesLoader.instance
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

    fun getTestAddress(seed: String) = iotaAPI.getNextAvailableAddress(seed, nodeSecurity, false).first()

    fun getTransactions(seed: String, tag: Tag?): List<Transaction> {
        logger.info("getTransactions for seed: $seed")
        val transferResponse = try {
            iotaAPI.getTransfers(seed, nodeSecurity, 0, 99, false)
        } catch (e: ArgumentException) {
            logger.error("Invalid parameters supplied for getTransactions, likely invalid seed", e)
            null
        }
        var transactions = transferResponse?.transfers?.flatMap { it.transactions } ?: listOf()
        tag?.let {
            transactions = transactions.filter {
                getASCIIFromTrytes(it.tag) == tag.name
            }
        }
        transactions = transactions.filter { !pt.hashStoredInDB(it.hash) }
        transactions.forEach { pt.saveHash(it.hash) }
        return transactions
    }

    fun getMessagesUnchecked(seed: String, tag: Tag?): List<String> { //does not compare signatures
        val transactions = getTransactions(seed, tag)
        return transactions.mapNotNull { getASCIIFromTrytes(it.signatureFragments) }
    }

    fun getNewestMessageSortedByTimeStamp(seed: String, tag: Tag?): String {
        val transactions = getTransactions(seed, tag)
        return transactions
            .sortedBy { it.timestamp }
            .mapNotNull { getASCIIFromTrytes(it.signatureFragments) }.first()
    }

    fun attachBroadcastToTangle(seed: String, message: String, tag: Tag): SendTransferResponse? {
        return attachBroadcastToTangle(seed, message, tag.name)
    }

    fun attachBroadcastToTangle(seed: String, message: String, tag: String): SendTransferResponse? {
        logger.info("Attaching transaction to tangle, seed: $seed\nmessage: $message\ntag:$tag")
        val messageTrytes = TrytesConverter.asciiToTrytes(message)
        val tagTrytes = TrytesConverter.asciiToTrytes(tag)
        val transfer =
            Transfer(iotaAPI.getNextAvailableAddress(seed, nodeSecurity, false).first(), 0, messageTrytes, tagTrytes)
        return try {
            val r = iotaAPI.sendTransfer(
                seed, nodeSecurity, 9, nodeMinWeightMagnitude, listOf(transfer), null, iotaAPI.getNextAvailableAddress(seed, nodeSecurity, false).first(),
                false, false, null
            )
            r?.let { it.transactions.forEach { pt.saveHash(it.hash) } }
            r
        } catch (e: Exception) {
            logger.error(e.toString())
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

    fun attachTransactionToTangle(seed: String, message: String, tag: Tag, addressTo: String): SendTransferResponse? {
        logger.info("Attaching transaction to tangle, seed: $seed\nmessage: $message\ntag:$tag")
        val messageTrytes = TrytesConverter.asciiToTrytes(message)
        val tagTrytes = TrytesConverter.asciiToTrytes(tag.name)
        val transfer =
            Transfer(addressTo, 0, messageTrytes, tagTrytes)
        return try {
            //pt.saveHash(transfer.hash)
            iotaAPI.sendTransfer(
                seed, nodeSecurity, 9, nodeMinWeightMagnitude, listOf(transfer), null, iotaAPI.getNextAvailableAddress(seed, nodeSecurity, false).first(),
                false, false, null
            )
        } catch (e: Exception) {
            println(e.message)
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
        val deviceSpecificationTagTrytes = TrytesConverter.asciiToTrytes(Tag.DSPEC.name)
        val transfer = Transfer(iotaAPI.getNextAvailableAddress(seed, nodeSecurity, false).first(), 0, messageTrytes, deviceSpecificationTagTrytes)
        return try {
            iotaAPI.sendTransfer(
                seed, nodeSecurity, 9, nodeMinWeightMagnitude, listOf(transfer), null,
                iotaAPI.getNextAvailableAddress(seed, nodeSecurity, false).first(), false, false, null

            )
        } catch (e: Exception) {
            logger.error(e.toString())
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
        return sorted.firstOrNull { transaction -> parseAndVerifyMessage(getASCIIFromTrytes(transaction.signatureFragments)!!, publicKey) }
    }

    fun getBroadcastsUnchecked(tag: Tag): List<Pair<String, String>> {
        logger.info("getNewestBroadcast tag: $tag")
        val transactions =
            iotaAPI.findTransactionObjectsByTag(arrayOf(TrytesConverter.asciiToTrytes(tag.name)))
        return transactions.mapNotNull { t -> getASCIIFromTrytes(t.signatureFragments)?.let { Pair(it, t.address) }}
    }

    fun getNewestBroadcasts(entityName: String, publicKey: String): List<Transaction>? {
        logger.info("getNewestBroadcasts entityName: $entityName publicKey: $publicKey")
        val transactions =
            iotaAPI.findTransactionObjectsByTag(arrayOf(entityName))
        return transactions.sortedByDescending { it.timestamp }.toList().filter { transaction -> parseAndVerifyMessage(getASCIIFromTrytes(transaction.signatureFragments)!!, publicKey) }
    }

    private fun parseAndVerifyMessage(messageASCII: String, publicKey: String): Boolean {
        logger.info("parsing and verifying message : $messageASCII $publicKey")
        val signature = messageASCII.substringAfter("__")
        val message = messageASCII.substringBefore("__")
        val publicECKey = EncryptionHelper.loadPublicECKeyFromProperties(publicKey)
        return EncryptionHelper.verifySignatureBase64(publicECKey, message, signature)
    }

    private fun parseAndVerifyMessageStringKey(messageASCII: String, publicKey: String): Boolean {
        logger.info("parsing and verifying message : $messageASCII $publicKey")
        val signature = messageASCII.substringAfter("__")
        val message = messageASCII.substringBefore("__")
        val publicECKey = EncryptionHelper.loadPublicECKeyFromBigInteger(BigInteger(publicKey))
        return EncryptionHelper.verifySignatureBase64(publicECKey, message, signature)
    }

    private fun getASCIIFromTrytes(trytes: String): String? {
        val paddedTrytes = (trytes.length % 2).let { if (it == 1) trytes + "9" else trytes }
        return try {
            TrytesConverter.trytesToAscii(paddedTrytes).trim((0).toChar())
        } catch (e: ArgumentException) {
            logger.error("Unable to convert - invalid trytes: $trytes")
            null
        }
    }

    fun getPendingMethodCalls(seed: String, procurations: List<Procuration>): List<PostMessage> {
        logger.info("getting Pending Method Calls, seed: $seed procurations: $procurations")
        val transactions = getTransactions(seed, Tag.MC)
        val postMessages = transactions.mapNotNull { transaction ->
            getASCIIFromTrytes(transaction.signatureFragments)?.let { ascii ->
                val signature = ascii.substringAfter("__")
                val message = ascii.substringBefore("__")
                Pair(PostMessageHack(gson.fromJson(message, PostMessage::class.java), ascii), signature)
            }
        }
        val verifiedMessages = postMessages.filter { m ->
            procurations.find { p -> p.messageChainID == m.first.postMessage.messageChainID }?.recipientPublicKey.toString().let {
                parseAndVerifyMessageStringKey(m.first.json, it)
            }
        }
        return verifiedMessages.map { it.first.postMessage }
    }
}