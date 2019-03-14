package Integrations.nordpool

import Tangle.TangleController
import com.google.gson.Gson
import datatypes.nordpool.IntervalItem
import datatypes.nordpool.NordPoolAPIMockResponse
import datatypes.nordpool.PublicationTimeSeries
import helpers.EncryptionHelper
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory

class NordPoolAPIMock(
    private val logger: Logger = SimpleLoggerFactory().getLogger("NordPoolAPIMock")
) {

    private val seed = "TEST99999999999999999999999999999999999999999999999999999999999999999999999999999"
    private val tangleController = TangleController()
    private val nordPoolAPIMockResponse = loadMockResponse()
    private val gson = Gson()

    fun publishMockPrices() {
        val chunks = nordPoolAPIMockResponse.publicationTimeSeries.period.interval
            .chunked(12)

        val timeSeries = nordPoolAPIMockResponse.publicationTimeSeries

        val copy = timeSeries.copyWithPeriodChunks(chunks[0])
        val copy2 = timeSeries.copyWithPeriodChunks(chunks[1])
        val signedResponse1 = signMockResponse(gson.toJson(copy))
        val signedResponse2 = signMockResponse(gson.toJson(copy2))
        logger.info("Attaching to tangle: $signedResponse1")
        tangleController.attachTransactionToTangle(seed, signedResponse1, "NP")
        logger.info("Attaching to tangle: $signedResponse2")
        tangleController.attachTransactionToTangle(seed, signedResponse2, "NP")
    }

    private fun loadMockResponse(): NordPoolAPIMockResponse {
        logger.info("Loading mock response NordPool.json")
        val mockFile = ClassLoader.getSystemResourceAsStream("NordPool.json")
        val readText = mockFile.bufferedReader().readText()
        return Gson().fromJson(readText, NordPoolAPIMockResponse::class.java)
    }

    private fun signMockResponse(json: String): String {
        logger.info("Signing mock response")
        val privateECKey = EncryptionHelper.loadPrivateECKey("nordPoolPrivateKey")
        return json + "__" + EncryptionHelper.signBase64(privateECKey, json)
    }
}

fun PublicationTimeSeries.copyWithPeriodChunks(chunks: List<IntervalItem>) = PublicationTimeSeries(currency, measureUnitPrice, period.copy(period.timeInterval, period.resolution, chunks))

