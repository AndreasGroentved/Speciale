package Integrations.nordpool

import Tangle.TangleController
import com.google.gson.Gson
import datatypes.nordpool.IntervalItem
import datatypes.nordpool.NordPoolAPIMockResponse
import datatypes.nordpool.PublicationTimeSeries
import helpers.EncryptionHelper
import java.io.File
import java.io.FileReader

class NordPoolAPIMock {
    private val seed = "TEST99999999999999999999999999999999999999999999999999999999999999999999999999999"
    private val tangleController = TangleController()
    private val nordPoolAPIMockResponse = loadMockResponse()
    private val gson = Gson()

    fun publishMockPrices() {
        val chunks = nordPoolAPIMockResponse.publicationTimeSeries?.period?.interval
            ?.chunked(12)?.map { it as List<*> } as? List<List<IntervalItem>>
            ?: throw RuntimeException("invalid server response")

        val timeSeries = nordPoolAPIMockResponse.publicationTimeSeries!!

        var copy = timeSeries.copyWithPeriodChunks(chunks[0])
        var copy2 = timeSeries.copyWithPeriodChunks(chunks[1])
        val signedResponse1 = signMockResponse(gson.toJson(copy))
        val signedResponse2 = signMockResponse(gson.toJson(copy2))
        println(tangleController.attachTransactionToTangle(seed, signedResponse1, "NP"))
        println(tangleController.attachTransactionToTangle(seed, signedResponse2, "NP"))
    }

    private fun loadMockResponse(): NordPoolAPIMockResponse {
        val mockFile = ClassLoader.getSystemResource("NordPool.json")
        val fileReader = FileReader(File(mockFile.toURI()))
        val readText = fileReader.readText()
        return Gson().fromJson(readText, NordPoolAPIMockResponse::class.java)
    }

    private fun signMockResponse(json: String): String {
        val privateECKey = EncryptionHelper.loadPrivateECKey("nordPoolPrivateKey")
        return json + "__" + EncryptionHelper.signBase64(privateECKey, json)
    }
}

fun PublicationTimeSeries.copyWithPeriodChunks(chunks: List<IntervalItem>) = PublicationTimeSeries(currency, measureUnitPrice, period!!.copy(period!!.timeInterval, period!!.resolution, chunks))

