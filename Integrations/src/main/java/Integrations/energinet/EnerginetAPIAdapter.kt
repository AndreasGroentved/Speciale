package Integrations.energinet

import Tangle.TangleController
import com.google.gson.Gson
import datatypes.energinet.EnerginetAPIResponse
import helpers.EncryptionHelper
import helpers.LogI
import org.slf4j.simple.SimpleLoggerFactory
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.Charset

class EnerginetAPIAdapter {

    private val gson = Gson()
    private val seed = "TESTA9999999999999999999999999999999999999999999999999999999999999999999999999999"
    private val tangleController = TangleController(seed)

    fun publishCO2Signal(retries: Int) {
        if (retries == 3) return
        LogI("Publishing CO2 Signal, try number: $retries")
        val encoded = URLEncoder.encode("select * from \"co2emis\" as emis where emis.\"Minutes5UTC\" >= now() - INTERVAL '10 min' limit 2", Charset.defaultCharset())
        val httpRequest = HttpRequest.newBuilder().uri(URI("https://api.energidataservice.dk//datastore_search_sql?sql=$encoded"))
            .GET()
            .build()
        val httpResponse = HttpClient.newHttpClient().send(
            httpRequest, HttpResponse.BodyHandlers.ofString()
        )
        val success = httpResponse.statusCode() == 200
        if (success) {
            LogI("Recieved Energinet result: " + httpResponse.body())
            val fromJson = gson.fromJson(httpResponse.body(), EnerginetAPIResponse::class.java)
            val toJson = gson.toJson(fromJson)
            val privateECKey = EncryptionHelper.loadPrivateECKeyFromProperties("energinetPrivateKey")
            val signed = toJson + "__" + EncryptionHelper.signBase64(privateECKey, toJson)
            LogI("Attaching to tangle: $signed")
           println(tangleController.attachBroadcastToTangle(signed, "EN"))
        } else publishCO2Signal(retries + 1)
    }

}