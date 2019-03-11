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

        val timeSeries = nordPoolAPIMockResponse.publicationTimeSeries
        val period = timeSeries?.period ?: throw RuntimeException("invalid period")

        val copy = timeSeries.copyWithPeriodChunks(chunks[0])
        val copy2 = timeSeries.copyWithPeriodChunks(chunks[1])
        signMockResponse(copy)
        signMockResponse(copy2)
        println(tangleController.attachTransactionToTangle(seed, gson.toJson(copy), "NP"))
        println(tangleController.attachTransactionToTangle(seed, gson.toJson(copy2), "NP"))
    }

    private fun loadMockResponse(): NordPoolAPIMockResponse {
        val mockFile = ClassLoader.getSystemResource("NordPool.json")
        val fileReader = FileReader(File(mockFile.toURI()))
        val readText = fileReader.readText()
        return Gson().fromJson(readText, NordPoolAPIMockResponse::class.java)
    }

    private fun signMockResponse(timeSeries: PublicationTimeSeries) {
        val privateECKey = EncryptionHelper.loadPrivateECKey("nordPoolPrivateKey")
        timeSeries.signature = EncryptionHelper.signBase64(privateECKey, "nordpool")
    }
}

fun PublicationTimeSeries.copyWithPeriodChunks(chunks: List<IntervalItem>) = PublicationTimeSeries(signature, currency, measureUnitPrice, period!!.copy(period!!.timeInterval, period!!.resolution, chunks))

