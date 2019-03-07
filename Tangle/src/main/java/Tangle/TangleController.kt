package Tangle

import Helpers.PropertiesLoader
import jota.IotaAPI
import jota.dto.response.SendTransferResponse
import jota.model.Transaction
import jota.model.Transfer
import jota.utils.TrytesConverter

class TangleController {

    private var deviceSpecificationTag: String? = null
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
        val properties = PropertiesLoader().loadProperties()
        deviceSpecificationTag = properties.getProperty("deviceSpecificationTag")
        nodeAddress = properties.getProperty("nodeAddress")
        nodePort = properties.getProperty("nodePort")
        nodeSecurity = properties.getProperty("nodeSecurity").toInt()
        nodeMinWeightMagnitude = properties.getProperty("nodeMinWeightMagnitude").toInt()
        nodeTrustedAddresses["Energinet"] = properties.getProperty("trustedAddressEnerginet")
        nodeTrustedAddresses["NordPool"] = properties.getProperty("trustedAddressNordPool")
        return properties.getProperty("nodeDefault")!!.toBoolean()
    }

    private fun initIotaAPI(isDefault: Boolean) {
        if (!isDefault) {
            iotaAPI = IotaAPI.Builder().host(nodeAddress).port(nodePort).build() //whatever is in the config
        } else {
            IotaAPI.Builder().build() //https://nodes.devnet.iota.org:443
        }

    }

    fun getTransactions(seed: String): MutableList<Transaction> {
        val transferResponse = iotaAPI!!.getTransfers(seed, nodeSecurity, 0, 5, false)
        val transactions: MutableList<Transaction> = mutableListOf()
        transferResponse.transfers.forEach { bundle -> transactions.addAll(bundle.transactions) }
        return transactions

    }

    fun attachTransactionToTangle(seed: String, message: String): SendTransferResponse? {
        val messageTrytes = TrytesConverter.asciiToTrytes(message)
        val transfer =
            Transfer(iotaAPI!!.getNextAvailableAddress(seed, nodeSecurity, false).first(), 0, messageTrytes, "")
        return iotaAPI!!.sendTransfer(
            seed,
            nodeSecurity,
            9,
            nodeMinWeightMagnitude,
            arrayListOf(transfer),
            null,
            iotaAPI!!.getNextAvailableAddress(seed, nodeSecurity, false).first(),
            false,
            false,
            null
        )
    }

    fun attachDeviceToTangle(seed: String, deviceSpecificationJson: String): SendTransferResponse? {
        val messageTrytes = TrytesConverter.asciiToTrytes(deviceSpecificationJson)
        val deviceSpecificationTagTrytes = TrytesConverter.asciiToTrytes(deviceSpecificationTag)
        val transfer = Transfer(
            iotaAPI!!.getNextAvailableAddress(seed, nodeSecurity, false).first(),
            0,
            messageTrytes,
            deviceSpecificationTagTrytes
        )
        return iotaAPI!!.sendTransfer(
            seed,
            nodeSecurity,
            9,
            nodeMinWeightMagnitude,
            arrayListOf(transfer),
            null,
            iotaAPI!!.getNextAvailableAddress(seed, nodeSecurity, false).first(),
            false,
            false,
            null
        )
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