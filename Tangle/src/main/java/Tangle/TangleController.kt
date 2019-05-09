package Tangle

import com.google.gson.Gson
import datatypes.iotdevices.PostMessage
import datatypes.iotdevices.PostMessageHack
import datatypes.iotdevices.Procuration
import datatypes.tangle.Tag
import helpers.*
import jota.IotaAPI
import jota.dto.response.SendTransferResponse
import jota.error.ArgumentException
import jota.model.Transaction
import jota.model.Transfer
import jota.utils.TrytesConverter
import org.apache.commons.lang3.time.StopWatch
import repositories.ProcessedTransactions
import java.math.BigInteger

class TangleController(private val seed: String, private val ip: String = "") { //IF IT DOES NOT WORK CHECK ADDRESS INDEX FROM & TO IN GETTRANSACTIONS

    private lateinit var nodeAddress: String
    private lateinit var nodePort: String
    private var nodeSecurity = 2
    private var nodeMinWeightMagnitude = 14
    private lateinit var iotaAPI: IotaAPI
    private val gson = Gson()
    private val pt = ProcessedTransactions
    private lateinit var address: String

    init {
        initIotaAPI(loadProperties())
    }

    private fun loadProperties(): Boolean {
        LogI("Loading properties")
        val properties = PropertiesLoader.instance
        nodeAddress = if (ip.isNotEmpty()) ip else properties.getProperty("nodeAddress")
        nodePort = properties.getProperty("nodePort")
        nodeSecurity = properties.getProperty("nodeSecurity").toInt()
        nodeMinWeightMagnitude = properties.getProperty("nodeMinWeightMagnitude").toInt()
        return properties.getProperty("nodeDefault").toBoolean()
    }

    private fun initIotaAPI(isDefault: Boolean) {
        if (!isDefault) {
            LogI("Building Iota API wrapper with custom properties")
            iotaAPI = IotaAPI.Builder().host(nodeAddress).port(nodePort).protocol("http").build() //whatever is in the config
        } else {
            LogI("Building Iota API wrapper with default values")
            IotaAPI.Builder().build() //https://nodes.devnet.iota.org:443
        }
        if (!PropertiesLoader.instance.hasProperty("tangleAddress")) {
            PropertiesLoader.instance.writeProperty("tangleAddress", generateAddress())
        }
        address = PropertiesLoader.instance.getProperty("tangleAddress")
    }

    fun getTransactions(tag: Tag?): List<Transaction> {
        val transferResponse = try {
            iotaAPI.getTransfers(seed, nodeSecurity, 0, 1, false)
        } catch (e: ArgumentException) {
            LogE("Invalid parameters supplied for getTransactions, likely invalid seed \n$e")
            null
        }
        StatisticsCollector.submitDuration("getTransactions", transferResponse!!.duration)
        var transactions = transferResponse.transfers?.flatMap { it.transactions } ?: listOf()
        tag?.let {
            transactions = transactions.filter {
                getASCIIFromTrytes(it.tag) == tag.name
            }
        }
        transactions = transactions.filter { !pt.hashStoredInDB(it.hash) }
        transactions.forEach { pt.saveHash(it.hash) }
        return transactions
    }

    fun getMessagesUnchecked(tag: Tag?): List<Pair<String, String>> { //does not compare signatures
        LogI("getting messages unchecked tag: $tag")
        val transactions = getTransactions(tag)
        return transactions.mapNotNull { t -> getASCIIFromTrytes(t.signatureFragments)?.let { a -> Pair(a, t.address) } }
    }

    fun generateAddress(): String = iotaAPI.getNextAvailableAddress(seed, nodeSecurity, false).first()


    fun attachBroadcastToTangle(message: String, tag: Tag): SendTransferResponse? = attachBroadcastToTangle(message, tag.name)


    fun attachBroadcastToTangle(message: String, tag: String): SendTransferResponse? {
        LogI("Attaching transaction to tangle, seed: $seed\nmessage: $message\ntag:$tag")
        val messageTrytes = TrytesConverter.asciiToTrytes(message)
        val tagTrytes = TrytesConverter.asciiToTrytes(tag)
        val transfer =
            Transfer(address, 0, messageTrytes, tagTrytes)
        return try {
            val t = iotaAPI.sendTransfer(
                seed, nodeSecurity, 9, nodeMinWeightMagnitude, listOf(transfer), null, address,
                false, false, null
            )
            StatisticsCollector.submitDuration("attachBroadcastToTangle", t.duration)
            t
        } catch (e: Exception) {
            LogE(e.toString())
            when (e) {
                is ArgumentException -> {
                    LogE("Invalid parameters supplied for sendTransfer");null
                }
                is IllegalStateException -> {
                    LogE("Cannot attach message to Tangle"); null
                }
                else -> throw e
            }
        }
    }

    fun attachTransactionToTangle(message: String, tag: Tag, addressTo: String): SendTransferResponse? {
        LogI("Attaching transaction to tangle, seed: $seed\nmessage: $message\ntag:$tag")
        val messageTrytes = TrytesConverter.asciiToTrytes(message)
        val tagTrytes = TrytesConverter.asciiToTrytes(tag.name)
        val transfer =
            Transfer(addressTo, 0, messageTrytes, tagTrytes)
        return try {
            val t = iotaAPI.sendTransfer(
                seed, nodeSecurity, 9, nodeMinWeightMagnitude, listOf(transfer), null, address,
                false, false, null
            )
            LogI(t.transactions.first().toTrytes())
            StatisticsCollector.submitDuration("attachTransactionToTangle", t.duration)
            t
        } catch (e: Exception) {
            when (e) {
                is ArgumentException -> {
                    LogE("Invalid parameters supplied for sendTransfer");null
                }
                is IllegalStateException -> {
                    LogE("Cannot attach message to Tangle"); null
                }
                else -> throw e
            }
        }
    }

    fun attachDeviceToTangle(tangleDeviceSpecification: String): SendTransferResponse? {
        LogI("Attaching device to tangle, seed: $seed\ndeviceSpecification: $tangleDeviceSpecification")
        val messageTrytes = TrytesConverter.asciiToTrytes(tangleDeviceSpecification)
        val deviceSpecificationTagTrytes = TrytesConverter.asciiToTrytes(Tag.DSPEC.name)
        val transfer = Transfer(address, 0, messageTrytes, deviceSpecificationTagTrytes)
        return try {
            val t = iotaAPI.sendTransfer(
                seed, nodeSecurity, 9, nodeMinWeightMagnitude, listOf(transfer), null,
                null, false, false, null
            )
            StatisticsCollector.submitDuration("attachDeviceToTangle", t.duration)
            t
        } catch (e: Exception) {
            LogE(e.toString())
            e.printStackTrace()
            when (e) {
                is ArgumentException -> {
                    LogE("Invalid parameters supplied for sendTransfer");null
                }
                is IllegalStateException -> {
                    LogE("Cannot attach message to Tangle"); null
                }
                else -> throw e
            }
        }
    }

    fun getNewestBroadcast(entityName: String, publicKey: String): String? {
        val stopWatch = StopWatch()
        stopWatch.start()
        LogI("getNewestBroadcast entityName: $entityName publicKey: $publicKey")
        return try {
            val transactions =
                iotaAPI.findTransactionObjectsByTag(arrayOf(TrytesConverter.asciiToTrytes(entityName)))
            stopWatch.stop()
            StatisticsCollector.submitDuration("getNewestBroadcast", stopWatch.time)
            val sorted = transactions.sortedByDescending { it.timestamp }.toList()
            val firstOrNull = sorted.firstOrNull { transaction -> parseAndVerifyMessage(getASCIIFromTrytes(transaction.signatureFragments)!!, publicKey) }
            getASCIIFromTrytes(firstOrNull?.signatureFragments!!.substringBefore("__"))

        } catch (e: Exception) {
            LogE(e.message)
            null
        }
    }

    fun getBroadcastsUnchecked(tag: Tag): List<Pair<String, String>> {
        val stopWatch = StopWatch()
        stopWatch.start()
        LogI("getNewestBroadcast tag: $tag")
        val transactions =
            iotaAPI.findTransactionObjectsByTag(arrayOf(TrytesConverter.asciiToTrytes(tag.name)))
        stopWatch.stop()
        StatisticsCollector.submitDuration("getBroadcastsUnchecked", stopWatch.time)
        val filter = transactions.filter { !pt.hashStoredInDB(it.hash) }
        //filter.forEach { pt.saveHash(it.hash) }
        return filter.mapNotNull { t -> getASCIIFromTrytes(t.signatureFragments)?.let { Pair(it, t.address) } }
    }

    fun getNewestBroadcasts(entityName: String, publicKey: String): List<Transaction>? {
        LogI("getNewestBroadcasts entityName: $entityName publicKey: $publicKey")
        val transactions =
            iotaAPI.findTransactionObjectsByTag(arrayOf(entityName))
        return transactions.sortedByDescending { it.timestamp }.toList().filter { transaction -> parseAndVerifyMessage(getASCIIFromTrytes(transaction.signatureFragments)!!, publicKey) }
    }

    private fun parseAndVerifyMessage(messageASCII: String, publicKey: String): Boolean {
        LogI("parsing and verifying message : $messageASCII $publicKey")
        val signature = messageASCII.substringAfter("__")
        val message = messageASCII.substringBefore("__")
        val publicECKey = EncryptionHelper.loadPublicECKeyFromProperties(publicKey)
        return EncryptionHelper.verifySignatureBase64(publicECKey, message, signature)
    }

    private fun parseAndVerifyMessageStringKey(messageASCII: String, publicKey: String): Boolean {
        LogI("parsing and verifying message : $messageASCII $publicKey")
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
            LogE("Unable to convert - invalid trytes: $trytes")
            null
        }
    }

    fun getPendingMethodCalls(procurations: List<Procuration>): List<PostMessageHack> {
        LogI("getting Pending Method Calls, seed: $seed procurations: $procurations")
        val transactions = getTransactions(Tag.MC)
        val postMessages = transactions.mapNotNull { transaction ->
            getASCIIFromTrytes(transaction.signatureFragments)?.let { ascii ->
                val signature = ascii.substringAfter("__")
                val message = ascii.substringBefore("__")
                Pair(PostMessageHack(gson.fromJson(message, PostMessage::class.java), ascii, transaction.address), signature)
            }
        }
        val verifiedMessages = postMessages.filter { (first) ->
            procurations.find { (messageChainID) -> messageChainID == first.postMessage.messageChainID }?.recipientPublicKey.toString().let {
                parseAndVerifyMessageStringKey(first.json, it)
            }
        }
        return verifiedMessages.map { it.first }
    }
}
