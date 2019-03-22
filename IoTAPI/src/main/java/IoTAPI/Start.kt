package IoTAPI

import DeviceManager.DeviceManager
import Tangle.TangleController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import datatypes.iotdevices.PostMessage
import datatypes.iotdevices.Procuration
import datatypes.iotdevices.ProcurationAck
import datatypes.tangle.Tag
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
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class IoTAPI {
    private val hs = HouseRules()
    private val seed = "TESTQ9999999999999999999999999999999999999999999999999999999999999999999999999999"
    private val logger = SimpleLoggerFactory().getLogger("IoTAPI")
    private val deviceManger = DeviceManager()
    private val gson = Gson()
    private val procurations = AcceptedProcurations()
    private val pendingProcurations: MutableList<Procuration> = mutableListOf()
    private val tangleController = TangleController()
    private val threadPool = ScheduledThreadPoolExecutor(1)
    private val pendingMethodCalls = mutableListOf<PostMessage>()

    fun start() {
        deviceManger.startDiscovery()
        val privateKey: PrivateKey
        val publicKey: PublicKey
        threadPool.scheduleAtFixedRate({ methodTask() }, 0, 20, TimeUnit.SECONDS)
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
            deviceManger.getActivePendingProcurations(procurations.getAllProcurations(), seed, tangleController)
        }

        put("/device/procuration/:id/accept") { request, response ->
            response.type("application/json")
            val id = request.params().get(":id")
            id?.let { pendingProcurations.find { it.messageChainID == id }?.let { respondToProcuration(it, true, seed, tangleController, privateKey) } }
        }
        //TODO: REVISIT DE HER, PATH ER LIDT WANK
        put("/device/procuration/:id/reject") { request, response ->
            response.type("application/json")
            val id = request.params().get(":id")
            id?.let { pendingProcurations.find { it.messageChainID == id }?.let { respondToProcuration(it, true, seed, tangleController, privateKey) } }
        }

        get("/device/procurations/accepted") { _, response ->
            response.type("application/json")
            procurations.getAllProcurations()
        }

        get("/device/procurations/expired") { _, response ->
            response.type("application/json")
            deviceManger.getExpiredProcurations(seed, tangleController)
        }

        get("/device/:id") { request, _ -> deviceManger.getDevice(request.params(":id")) }
        get("/device/:id/:path") { request, response ->
            val id = request.params(":id")
            val path = request.params(":path")
            val params = if (request.queryParams().isNotEmpty()) "?" + request.queryParams().map { request.params(it) } else ""
            response.type("application/json")
            val ret = deviceManger.get(PostMessage("this", id, path, params))
            ret
        }

        post("/device/:id/:path") { request, _ ->
            val id = request.params(":id")
            val path = request.params(":path")
            deviceManger.post(PostMessage("this", id, "POST", path, getParameterMap(request.body())))
        }

        get("/device/:id/price") { request, _ ->
            //TODO
            val from = request.params(":from").toLong()
            val to = request.params(":to").toLong()
            val id = request.params(":id").toString()
            deviceManger.getSavingsForDevice(from, to, id, tangleController)
        }

        get("/device/:id/time") { request, _ ->
            //TODO ????? skal der laves noget her?
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
            deviceManger.getAllSavings(from, to, tangleController)
        }

        delete("/device/:id") { request, _ ->
            val id = request.params(":id")
            deviceManger.unregisterDevice(privateKey, seed, id, tangleController)
        }

        put("/device/:id") { request, _ ->
            val id = request.params(":id")
            deviceManger.registerDevice(privateKey, BigInteger(publicKey.encoded).toString(), id, seed, tangleController)
        }

        post("tangle/permissioned/devices") { request, _ ->
            val postMessage = gson.fromJson(request.body(), PostMessage::class.java)
            sendMethodCall(postMessage, privateKey, "")
        }

        get("tangle/unpermissioned/devices") { _, _ ->
            tangleController.getMessagesUnchecked(seed, Tag.DSPEC)
        }

        //TODO: OVERVEJ MESSAGE DESIGN HER + navnet recipientPublicKey + !!
        post("tangle/unpermissioned/devices/procuration") { request, _ ->
            val params = getParameterMap(request.body())
            val dateFrom = gson.fromJson(params["dateFrom"], Date::class.java)
            val dateTo = gson.fromJson(params["dateTo"], Date::class.java)
            requestProcuration(
                Procuration(
                    UUID.randomUUID().toString(), params["deviceID"]!!, BigInteger(params["recipientPublicKey"]),
                    dateTo, dateFrom
                ), params["addressTo"]!!, tangleController, privateKey
            )
        }

    }

    fun methodTask() {
        pendingMethodCalls += tangleController.getPendingMethodCalls(seed, procurations.getAllProcurations())
        logger.info("$pendingMethodCalls")
        pendingMethodCalls.forEach { handleMethodType(it) }
    }

    fun handleMethodType(message: PostMessage) {
        logger.info(message.type)
        when (message.type.toLowerCase()) {
            "get" -> logger.info(deviceManger.get(message))
            "post" -> deviceManger.post(message)
            else -> logger.error("method type unsupported ${message.type}")
        }

    }

    fun sendMethodCall(postMessage: PostMessage, privateKey: PrivateKey, addressTo: String) {
        val toJson = Gson().toJson(postMessage)
        val signBase64 = EncryptionHelper.signBase64(privateKey, toJson)
        tangleController.attachTransactionToTangle(seed, toJson + "__" + signBase64, Tag.MC, addressTo)
    }

    fun respondToProcuration(procuration: Procuration, accepted: Boolean, seed: String, tangle: TangleController, privateKey: PrivateKey) {
        val procurationAck = ProcurationAck(procuration.messageChainID, accepted)
        val json = gson.toJson(procurationAck)
        val signBase64 = EncryptionHelper.signBase64(privateKey, json)
        tangle.attachBroadcastToTangle(seed, json + "__" + signBase64, Tag.PROACK)
    }

    fun requestProcuration(procuration: Procuration, addressTo: String, tangle: TangleController, privateKey: PrivateKey) {
        val json = gson.toJson(procuration)
        val signBase64 = EncryptionHelper.signBase64(privateKey, json)
        tangle.attachTransactionToTangle(seed, json + "__" + signBase64, Tag.PRO, addressTo)
    }
}

fun main() {
    IoTAPI().start()
}