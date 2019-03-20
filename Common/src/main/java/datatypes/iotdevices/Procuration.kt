package datatypes.iotdevices

import java.math.BigInteger
import java.util.*

data class Procuration(val messageChainID: String, val deviceID: String, val recipientPublicKey: BigInteger, val dateFrom: Date, val dateTo: Date)