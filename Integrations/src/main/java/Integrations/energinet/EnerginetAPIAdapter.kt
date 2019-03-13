package Integrations.energinet

import Tangle.TangleController
import com.google.gson.Gson
import datatypes.energinet.EnerginetAPIResponse
import helpers.EncryptionHelper
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.Charset

class EnerginetAPIAdapter {

    private val gson = Gson()
    private val seed = "TESTA9999999999999999999999999999999999999999999999999999999999999999999999999999"
    private val tangleController = TangleController()

    fun publishCO2Signal() {
        val encoded = URLEncoder.encode("select * from \"co2emis\" as emis where emis.\"Minutes5UTC\" >= now() - INTERVAL '10 min' limit 2", Charset.defaultCharset())
        val httpRequest = HttpRequest.newBuilder().uri(URI("https://api.energidataservice.dk//datastore_search_sql?sql=" + encoded))
            .GET()
            .build()
        val httpResponse = HttpClient.newHttpClient().send(
            httpRequest, HttpResponse.BodyHandlers.ofString()
        )
        val fromJson = gson.fromJson(httpResponse.body(), EnerginetAPIResponse::class.java)
        val toJson = gson.toJson(fromJson)
        val privateECKey = EncryptionHelper.loadPrivateECKey("energinetPrivateKey")
        val signed = toJson + "__" + EncryptionHelper.signBase64(privateECKey, toJson)
        tangleController.attachTransactionToTangle(seed, signed, "EN")
    }

}