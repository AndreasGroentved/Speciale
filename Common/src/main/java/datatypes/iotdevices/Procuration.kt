package datatypes.iotdevices

import java.math.BigInteger
import java.util.*

data class Procuration(val messageChainID: String = "", val deviceID: String = "", val recipientPublicKey: BigInteger = BigInteger("0"), val dateFrom: Date = Date(0), val dateTo: Date = Date(0))