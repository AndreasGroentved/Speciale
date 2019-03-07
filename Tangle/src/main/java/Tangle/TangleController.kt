package Tangle

import jota.IotaAPI
import jota.dto.response.GetTransferResponse
import jota.model.Transaction
import java.util.*
import kotlin.collections.HashMap

class TangleController {

    private var nodeAddress: String? = null
    private var nodePort: String? = null
    private var nodeSecurity: Int = 2
    private var nodeMinWeightMagnitude: Int = 14
    private var nodeTrustedAddresses: HashMap<String, String> = HashMap()
    private var iotaAPI: IotaAPI? = null

    init {
        val loadProperties = loadProperties()
        initIotaAPI(loadProperties)
    }

    private fun loadProperties(): Boolean {
        val properties = Properties()
        try {
            val resourceAsStream = TangleController::class.java.getResourceAsStream("properties.config")
            properties.load(resourceAsStream)
            nodeAddress = properties.getProperty("nodeAddress")
            nodePort = properties.getProperty("nodePort")
            nodeSecurity = properties.getProperty("nodeSecurity").toInt()
            nodeMinWeightMagnitude = properties.getProperty("nodeMinWeightMagnitude").toInt()
            nodeTrustedAddresses["Energinet"] = properties.getProperty("trustedAddressEnerginet")
            nodeTrustedAddresses["NordPool"] = properties.getProperty("trustedAddressNordPool")
            val isDefault = properties.getProperty("nodeDefault")!!.toBoolean()
            resourceAsStream.close()
            return isDefault
        } catch (e: Exception) {
            println("Could not read file config.properties")
            e.printStackTrace()
            throw e
        }
    }

    private fun initIotaAPI(isDefault: Boolean) {
        if (!isDefault) {
            iotaAPI = IotaAPI.Builder().host(nodeAddress).port(nodePort).build() //whatever is in the config
        } else {
            IotaAPI.Builder().build() //https://nodes.devnet.iota.org:443
        }

    }

    fun getTransactions(seed: String): GetTransferResponse? {
        return iotaAPI!!.getTransfers(seed, nodeSecurity, 0, 5, false)
    }

    fun attachTransactionToTangle(vararg trytes: String): MutableList<Transaction>? {
        return iotaAPI!!.sendTrytes(trytes, 3, nodeMinWeightMagnitude, null)
    }

    fun attachDeviceToTangle() {

    }

    fun getNewestBroadcast(entityName: String): Transaction? {
        val findTransactionsResponse =
            iotaAPI!!.findTransactions(arrayOf(nodeTrustedAddresses[entityName]), null, null, null)
        val getTrytesResponse = iotaAPI!!.getTrytes(*findTransactionsResponse.hashes)
        var transactions = getTrytesResponse.trytes.map { trytes -> Transaction(trytes) }

        transactions = transactions.sortedBy { tx -> tx.timestamp }

        if (transactions.isNotEmpty()) {
            return transactions[0]
        }
        return null
    }

}