package Tangle

import com.google.gson.Gson
import datatypes.nordpool.PublicationTimeSeries
import helpers.EncryptionHelper
import Helpers.PropertiesLoader
import jota.IotaAPI
import jota.dto.response.SendTransferResponse
import jota.model.Transaction
import jota.model.Transfer
import jota.utils.TrytesConverter
import java.math.BigDecimal

class TangleController {

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
        val properties = PropertiesLoader.instance
        deviceSpecificationTag = properties.getProperty("deviceSpecificationTag")
        nodeAddress = properties.getProperty("nodeAddress")
        nodePort = properties.getProperty("nodePort")
        nodeSecurity = properties.getProperty("nodeSecurity").toInt()
        nodeMinWeightMagnitude = properties.getProperty("nodeMinWeightMagnitude").toInt()
        return properties.getProperty("nodeDefault")!!.toBoolean()
    }

    private fun initIotaAPI(isDefault: Boolean) {
        if (!isDefault) {
            iotaAPI = IotaAPI.Builder().host(nodeAddress).port(nodePort).build() //whatever is in the config
        } else {
            IotaAPI.Builder().build() //https://nodes.devnet.iota.org:443
        }

    }

    fun getTransactions(seed: String): List<Transaction> {
        val transferResponse = iotaAPI.getTransfers(seed, nodeSecurity, 0, 5, false)
        return transferResponse.transfers.flatMap { it.transactions }
    }

    fun attachTransactionToTangle(seed: String, message: String, tag: String): SendTransferResponse? {
        val messageTrytes = TrytesConverter.asciiToTrytes(message)
        val transfer =
            Transfer(iotaAPI.getNextAvailableAddress(seed, nodeSecurity, false).first(), 0, messageTrytes, tag)
        return iotaAPI.sendTransfer(
            seed, nodeSecurity, 9, nodeMinWeightMagnitude, listOf(transfer), null, iotaAPI.getNextAvailableAddress(seed, nodeSecurity, false).first(),
            false, false, null
        )
    }

    fun attachDeviceToTangle(seed: String, deviceSpecificationJson: String): SendTransferResponse? {
        val messageTrytes = TrytesConverter.asciiToTrytes(deviceSpecificationJson)
        val deviceSpecificationTagTrytes = TrytesConverter.asciiToTrytes(deviceSpecificationTag)
        val transfer = Transfer(iotaAPI.getNextAvailableAddress(seed, nodeSecurity, false).first(), 0, messageTrytes, deviceSpecificationTagTrytes)
        return iotaAPI.sendTransfer(
            seed, nodeSecurity, 9, nodeMinWeightMagnitude, listOf(transfer), null,
            iotaAPI.getNextAvailableAddress(seed, nodeSecurity, false).first(), false, false, null
        )
    }

    fun getDevicePriceSavings(from: Long, to: Long, deviceId: String): BigDecimal {
        //TODO registerer slukket tidsperiode, find pris i den periode
        //TODO sammenlign pris med efterfølgende periode
        return BigDecimal(12)
    }

    fun getNewestBroadcast(entityName: String): Transaction? {
        val transactions =
            iotaAPI.findTransactionObjectsByTag(arrayOf(entityName))
        val sorted = transactions.sortedByDescending { it.timestamp }.toList()
        return sorted.first { transaction -> parseAndVerifyNordPool(transaction) }
    }

    fun getNewestBroadcasts(entityName: String): List<Transaction>? {
        val transactions =
            iotaAPI.findTransactionObjectsByTag(arrayOf(entityName))
        return transactions.sortedByDescending { it.timestamp }.toList().filter { transaction -> parseAndVerifyNordPool(transaction) }
    }

    private fun parseAndVerifyNordPool(transaction: Transaction): Boolean {
        val message = TrytesConverter.trytesToAscii(transaction.signatureFragments + "9")
        val messageTrimmed = message.trim((0).toChar())
        val response = Gson().fromJson(messageTrimmed, PublicationTimeSeries::class.java)
        val publicECKey = EncryptionHelper.loadPublicECKey("nordPoolPublicKey")
        println(EncryptionHelper.verifySignatureBase64(publicECKey, "nordpool", response.signature!!))
        return EncryptionHelper.verifySignatureBase64(publicECKey, "nordpool", response.signature!!)


    }

    private fun parse(transaction: Transaction) {

    }

}