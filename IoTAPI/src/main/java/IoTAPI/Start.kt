package IoTAPI

import DeviceManager.DeviceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import datatypes.iotdevices.PostMessage
import datatypes.iotdevices.Procuration
import helpers.EncryptionHelper
import helpers.PropertiesLoader
import org.slf4j.simple.SimpleLoggerFactory
import repositories.AcceptedProcurations
import spark.Filter
import spark.Request
import spark.Response
import spark.Spark
import spark.Spark.*
import java.math.BigInteger
import java.security.PrivateKey
import java.security.PublicKey

fun main() {
    val hs = HouseRules()

    val logger = SimpleLoggerFactory().getLogger("IoTAPI")
    val deviceManger = DeviceManager()
    val procurations = AcceptedProcurations()
    val pendingProcurations: MutableList<Procuration> = mutableListOf()
    deviceManger.startDiscovery()
    val privateKey: PrivateKey
    val publicKey: PublicKey
    if (PropertiesLoader.instance.getOptionalProperty("householdPrivateKey") == null || PropertiesLoader.instance.getOptionalProperty("householdPublicKey") == null) {
        val keyPair = EncryptionHelper.generateKeys()
        privateKey = keyPair.private
        publicKey = keyPair.public
        PropertiesLoader.instance.writeProperty("householdPrivateKey", BigInteger(privateKey.encoded).toString())
        PropertiesLoader.instance.writeProperty("householdPublicKey", BigInteger(publicKey.encoded).toString())
    } else {
        privateKey = EncryptionHelper.loadPrivateECKeyFromProperties("householdPrivateKey")
        publicKey = EncryptionHelper.loadPublicECKeyFromProperties("householdPublicKey")
    }

    fun getParameterMap(body: String): Map<String, String> {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return Gson().fromJson(body, mapType)
    }

    Spark.exception(Exception::class.java) { e, _, _ -> logger.error(e.toString()) }
    Spark.after("/*") { request, _ -> logger.info(request.pathInfo());logger.info(request.body()); logger.info(request.params().toString());logger.info(request.uri()) }

    options("/*") { request, response ->
        val accessControlRequestHeaders = request.headers("Access-Control-Request-Headers")
        if (accessControlRequestHeaders != null) {
            response.header(
                "Access-Control-Allow-Headers",
                accessControlRequestHeaders
            )
        }

        val accessControlRequestMethod = request.headers("Access-Control-Request-Method")
        if (accessControlRequestMethod != null) {
            response.header(
                "Access-Control-Allow-Methods",
                accessControlRequestMethod
            )
        }
        "OK"
    }

    before(Filter { _: Request, response: Response ->
        response.header("Access-Control-Allow-Origin", "*")
    })


    get("rule") { _, _ ->
        "{\"result\": \"" + hs.getRules().rule + "\"}"
    }

    post("rule") { request, _ ->
        val rule = getParameterMap(request.body())["rules"] ?: return@post "{\"error\":\"invalid json\"}"
        hs.saveRules(rule)
        "{\"Post\":\"Successful\"}"
    }

    get("/device") { request, response ->
        response.type("application/json")
        val params = request.queryString()
        deviceManger.getDevices(params)
    }

    get("/device/procurations/pending") { _, response ->
        response.type("application/json")
        deviceManger.getActivePendingProcurations(procurations.getAllProcurations())
    }

    put("/device/procuration/:id/accept") { request, response ->
        response.type("application/json")
        val id = request.params().get(":id")
        id?.let { pendingProcurations.find { it.messageChainID == id }?.let { deviceManger.respondToProcuration(it, true) } }
    }
    put("/device/procuration/:id/reject") { request, response ->
        response.type("application/json")
        val id = request.params().get(":id")
        id?.let { pendingProcurations.find { it.messageChainID == id }?.let { deviceManger.respondToProcuration(it, true) } }
    }

    get("/device/procurations/accepted") { _, response ->
        response.type("application/json")
        procurations.getAllProcurations()
    }

    get("/device/procurations/expired") { _, response ->
        response.type("application/json")
        deviceManger.getExpiredProcurations()
    }

    get("/device/:id") { request, _ -> deviceManger.getDevice(request.params(":id")) }
    get("/device/:id/:path") { request, response ->
        val id = request.params(":id")
        val path = request.params(":path")
        val params = if (request.queryParams().isNotEmpty()) "?" + request.queryParams().map { request.params(it) } else ""
        response.type("application/json")
        val ret = deviceManger.get(id, path, params)
        ret
    }

    post("/device/:id/:path") { request, _ ->
        val id = request.params(":id")
        val path = request.params(":path")
        deviceManger.post(id, path, request.body())
    }

    get("/device/:id/price") { request, _ ->
        //TODO
        val from = request.params(":from").toLong()
        val to = request.params(":to").toLong()
        val id = request.params(":id").toString()
        deviceManger.getSavingsForDevice(from, to, id)
    }

    get("/device/:id/time") { request, _ ->
        //TODO
        val from = request.queryParams(":from") as String
        val toTime = request.queryParams(":to") as String
        val id = request.params(":id") as String
        val postMessage = PostMessage("this", id, "get", "time", mapOf("from" to from, "to" to toTime))
        deviceManger.post(postMessage)
    }

    get("/price") { request, _ ->
        //todo
        val from = request.params(":from").toLong()
        val to = request.params(":to").toLong()
        deviceManger.getAllSavings(from, to)
    }

    delete("/device/:id") { request, _ ->
        val id = request.params(":id")
        deviceManger.unregisterDevice(id)
    }

    put("/device/:id") { request, _ ->
        val id = request.params(":id")
        deviceManger.registerDevice(privateKey, BigInteger(publicKey.encoded).toString(), id)
    }
}