package IoTAPI

import DeviceManager.DeviceManager
import Tangle.TangleController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import datatypes.iotdevices.*
import datatypes.tangle.Tag
import helpers.*
import org.slf4j.simple.SimpleLoggerFactory
import repositories.*
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
//todo: random location i know, men husk lige at man skulle kunne kalde get på tangle devices
class IoTAPI {
    private val hs = HouseRules()
    private val ruleManager = RuleManager()
    private val seed = "TESEA9999999999999999999999999999999999999999999999999999999999999999999999999999"
    private val seedTEST = "TESTQT999999999999999999999999999999999999999999999999999999999999999999999999999"
    private val log = SimpleLoggerFactory().getLogger("IoTAPI")
    private val deviceManger = DeviceManager()
    private val gson = Gson()
    private val procurations = AcceptedProcurations()
    private val pendingProcurations: MutableList<Procuration> = mutableListOf()
    private val tangleController = TangleController()
    private val threadPool = ScheduledThreadPoolExecutor(1)
    private val pendingMethodCalls = mutableListOf<PostMessageHack>()
    private lateinit var privateKey: PrivateKey
    private lateinit var publicKey: PublicKey
    private val messageRepo = MessageRepo()
    private val procurationAcks = ProcurationAcks()
    private val sentProcurations = SentProcurations()

    //TODO alle metoder skal altid returnere valid JSON
    fun start() {
        deviceManger.startDiscovery()
        threadPool.scheduleAtFixedRate({ methodTask() }, 0, 20, TimeUnit.SECONDS)
        threadPool.scheduleAtFixedRate({ procurationTask() }, 0, 20, TimeUnit.SECONDS)
        threadPool.scheduleAtFixedRate({ methodResponseTask() }, 0, 20, TimeUnit.SECONDS)
        Spark.port(PropertiesLoader.instance.getProperty("iotApiPort").toInt())

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
        /*   threadPool.scheduleAtFixedRate({
               requestProcuration(Procuration(UUID.randomUUID().toString(), "hest", BigInteger(publicKey.encoded), Date(), Date(1654523307558)), tangleController.getTestAddress(seed), tangleController, privateKey)
           }, 0, 20, TimeUnit.SECONDS)
           threadPool.scheduleAtFixedRate({ deviceManger.registerDevice(privateKey, BigInteger(publicKey.encoded).toString(), "hest", seedTEST, tangleController) }, 0, 20, TimeUnit.SECONDS)*/

        Spark.exception(Exception::class.java) { e, _, _ -> log.e(e) }
        Spark.after("/*") { request, _ -> LogI(request.requestMethod() + " " + request.uri() + " " + request.body() + " " + request.params().toString()) }

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
            gson.toJson(ClientResponse(hs.getRules().rule))
        }

        post("rule") { request, _ ->
            val rule = (getParameterMap(request.body())as? Map<String, String>?)?.get("rules")
                ?: return@post "{\"error\":\"invalid json\"}"
            hs.saveRules(rule)
            ruleManager.updateDsl(rule)
            "{\"result\":\"successful\"}"
        }

        get("/device") { request, response ->
            response.type("application/json")
            val params = request.queryString()
            val ret = deviceManger.getDevices(params)
            ret
        }

        get("/device/procurations/pending") { _, response ->
            //TODO wrap i ClientResponse
            response.type("application/json")
            val toJson = gson.toJson(getActivePendingProcurations(procurations.getAllProcurations()))
            toJson
        }

        put("/device/procuration/:id/accept") { request, response ->
            response.type("application/json")
            val id = request.params()[":id"]
            id?.let { pendingProcurations.find { it.messageChainID == id }?.let { respondToProcuration(it, true, seed, tangleController, privateKey) } }?.let { pendingProcurations.removeIf { p -> p.messageChainID == id } }
                ?: ""
        }

        //TODO: REVISIT DE HER, PATH ER LIDT WANK
        put("/device/procuration/:id/reject") { request, response ->
            response.type("application/json")
            val id = request.params()[":id"]
            id?.let { pendingProcurations.find { it.messageChainID == id }?.let { respondToProcuration(it, false, seed, tangleController, privateKey) } }?.let { pendingProcurations.removeIf { p -> p.messageChainID == id } }
                ?: ""
        }

        get("/device/procurations/accepted") { _, response ->
            response.type("application/json")
            gson.toJson(procurations.getAllProcurations())
        }

        get("/device/procurations/expired") { _, response ->
            response.type("application/json")
            gson.toJson(getExpiredProcurations())
        }




        get("/device/:id") { request, _ ->
            "{\"result\": " + gson.toJson(deviceManger.getDevice(request!!.params(":id"))!!.specification) + "}"
        }


        get("/device/:id/:path") { request, response ->
            val id = request.params(":id")
            val path = request.params(":path")
            val params = if (request.queryParams().isNotEmpty()) "?" + request.queryParams().map { request.params(it) } else ""
            response.type("application/json")
            deviceManger.get(PostMessage("this", deviceID = id, path = path + params))
        }

        post("/device/:id/:path")
        { request, _ ->
            val id = request.params(":id")
            val path = request.params(":path")
            deviceManger.post(PostMessage("this", id, "POST", path, getParameterMap(request.body()) as Map<String, String>))
        }

        get("/device/:id/time")
        { request, _ ->
            //TODO ????? skal der laves noget her?
            val from = request.queryParams(":from") as String
            val toTime = request.queryParams(":to") as String
            val id = request.params(":id") as String
            val postMessage = PostMessage("this", id, "get", "time", mapOf("from" to from, "to" to toTime))
            deviceManger.post(postMessage)
        }

        delete("/device/:id") { request, _ ->
            val id = request.params(":id")
            deviceManger.unregisterDevice(privateKey, seed, id, tangleController)
        }

        put("/device/:id") { request, _ ->
            val id = request.params(":id")
            deviceManger.registerDevice(privateKey, BigInteger(publicKey.encoded).toString(), id, seed, tangleController)
        }

        post("/tangle/permissioned/devices") { request, _ ->
            val postMessage = request.body().let {
                gson.fromJson(it, PostMessage::class.java)
            }
                ?: throw RuntimeException("invalid postMessage")
            // val addressTo = params["addressTo"] as? String ?: throw RuntimeException("invalid addressTo")

            println("yo")
            sendMethodCall(postMessage, privateKey, postMessage.addressTo)
            ClientResponse("success").let { gson.toJson(it) }
        }

        val requests = mutableMapOf<String, Pair<TangleDeviceSpecification, String>>()

        get("/tangle/permissioned/devices") { _, _ ->
            tangleController.getBroadcastsUnchecked(Tag.PROACK).mapNotNull {
                val proAck = gson.fromJson(it.first.substringBefore("__"), ProcurationAck::class.java)
                procurationAcks.saveProcuration(proAck)
                val mId = proAck.messageChainID
                requests[mId]

            }.let { ClientResponse(it) }.let { gson.toJson(it) }
        }



        get("/tangle/unpermissioned/devices") { _, _ ->
            val map = tangleController.getBroadcastsUnchecked(Tag.DSPEC).map {
                Pair(gson.fromJson(it.first.substringBefore("__"), TangleDeviceSpecification::class.java), it.second)
            }
            ClientResponse(map).let { gson.toJson(it) }
        }


        //TODO: OVERVEJ MESSAGE DESIGN HER + navnet recipientPublicKey + !!
        post("/tangle/unpermissioned/devices/procuration") { request, _ ->
            val params = getParameterMap(request.body()) as? Map<String, String>
                ?: throw RuntimeException("invalid params")
            val specification = params["specification"].let { gson.fromJson(it, TangleDeviceSpecification::class.java) }
            val dateFrom = params["dateFrom"]?.toLongOrNull() ?: throw RuntimeException("Invalid from date")
            val dateTo = params["dateTo"]?.toLongOrNull() ?: throw RuntimeException("Invalid to date")
            val messageId = UUID.randomUUID().toString()
            requests[messageId] = Pair(specification, params.getValue("addressTo"))
            requestProcuration(
                Procuration(
                    messageId, params.getValue("deviceId"), BigInteger(publicKey.encoded),
                    Date(dateTo), Date(dateFrom)
                ), params.getValue("addressTo"), tangleController, privateKey
            ).let { ClientResponse("yolo") }.let { gson.toJson(it) }
        }

        get("/tangle/messages/:deviceID") { request, _ ->
            request.params("deviceID")?.let { gson.toJson(messageRepo.getMessages(it).sortedByDescending { m -> m.timestamp }) }
        }

        get("/tangle/messagechainid/:deviceID") { request, _ ->
            request.params("deviceID")?.let { gson.toJson(sentProcurations.getProcurationDeviceID(it).messageChainID) }
        }

    }

    private fun methodTask() {
        pendingMethodCalls += tangleController.getPendingMethodCalls(seed, procurations.getAllProcurations())
        log.i(pendingMethodCalls)
        pendingMethodCalls.forEach { handleMethodType(it) }
    }


    private fun methodResponseTask() {
        //todo: definitely change to check signatures..
        val messagesUnchecked = tangleController.getMessagesUnchecked(seed, Tag.MR)
        messagesUnchecked.forEach {
            val responseWithDeviceID = gson.fromJson(it.substringBefore("__"), ResponseWithDeviceID::class.java)
            val procuration = sentProcurations.getProcurationDeviceID(responseWithDeviceID.deviceID)
            val validSignature = EncryptionHelper.verifySignatureBase64(EncryptionHelper.loadPublicECKeyFromBigInteger(procuration.recipientPublicKey), it.substringBefore("__"), it.substringAfter("__"))
            if (validSignature) {
                messageRepo.saveMessage(Message(it, Date(), responseWithDeviceID.deviceID))
            }
        }
    }

    private fun procurationTask() {
        tangleController.getMessagesUnchecked(seed, Tag.PROACK)
        LogI("$pendingMethodCalls")
        pendingMethodCalls.forEach { handleMethodType(it) }
    }

    private fun handleMethodType(message: PostMessageHack) {
        LogI("handling messsage: $message")
        val procuration = procurations.getProcuration(message.postMessage.messageChainID)
        val verifiedSignature = procuration?.let {
            EncryptionHelper.verifySignatureBase64(
                EncryptionHelper.loadPublicECKeyFromBigInteger(it.recipientPublicKey),
                message.json.substringBefore("__"), message.json.substringAfter("__")
            )
        }
        if(verifiedSignature == null || !verifiedSignature) {
            LogI("cannt verify message")
            return
        }
        val result = when (message.postMessage.type.toLowerCase()) {
            "get" -> gson.fromJson(deviceManger.get(message.postMessage), ResponseToClient::class.java)
            "post" -> gson.fromJson(deviceManger.post(message.postMessage), ResponseToClient::class.java)
            else -> ResponseToClient("ERROR method type not supported: ${message.postMessage.type}")
        }
        val response = gson.toJson(result)
        val signature = EncryptionHelper.signBase64(privateKey, response)
        tangleController.attachTransactionToTangle(seed, response + "__" + signature, Tag.MR, message.addressFrom)
    }

    private fun sendMethodCall(postMessage: PostMessage, privateKey: PrivateKey, addressTo: String) {
        val toJson = Gson().toJson(postMessage)
        val signBase64 = EncryptionHelper.signBase64(privateKey, toJson)
        tangleController.attachTransactionToTangle(seed, toJson + "__" + signBase64, Tag.MC, addressTo)
        messageRepo.saveMessage(Message(toJson, Date(), postMessage.deviceID))
    }

    //todo: maybe not broadcast
    private fun respondToProcuration(procuration: Procuration, accepted: Boolean, seed: String, tangle: TangleController, privateKey: PrivateKey) {
        val procurationAck = ProcurationAck(procuration.messageChainID, accepted)
        val json = gson.toJson(procurationAck)
        val signBase64 = EncryptionHelper.signBase64(privateKey, json)
        tangle.attachBroadcastToTangle(seed, json + "__" + signBase64, Tag.PROACK)?.let {
            procurations.saveProcuration(procuration)
            messageRepo.saveMessage(Message(json, Date(), procuration.deviceID))
        }
    }

    private fun requestProcuration(procuration: Procuration, addressTo: String, tangle: TangleController, privateKey: PrivateKey) {
        val json = gson.toJson(procuration)
        val signBase64 = EncryptionHelper.signBase64(privateKey, json)
        tangle.attachTransactionToTangle(seedTEST, json + "__" + signBase64, Tag.PRO, addressTo)?.let { messageRepo.saveMessage(Message(json, Date(), procuration.deviceID)) }
        sentProcurations.saveProcuration(procuration)
    }

    private fun getParameterMap(body: String): Map<String, Any> {
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(body, mapType)
    }

    private fun getActivePendingProcurations(accepted: List<Procuration>): List<Procuration> {
        val messages = tangleController.getMessagesUnchecked(seed, Tag.PRO)
        val procurations = messages.mapNotNull { m ->
            try {
                gson.fromJson(m.substringBefore("__"), Procuration::class.java)
            } catch (e: Exception) {
                null
            }
        }
        pendingProcurations.addAll(procurations.filter { p -> accepted.firstOrNull { a -> p.messageChainID == a.messageChainID } == null }.filter { p -> p.dateTo >= Date() })
        return pendingProcurations
    }

    private fun getExpiredProcurations(): List<Procuration> {
        /*
        val messages = tangleController.getMessagesUnchecked(seed, Tag.PROACK)
        return messages.mapNotNull { m ->
            try {
                gson.fromJson(m, Procuration::class.java)
            } catch (e: Exception) {
                null
            }
        }.filter { p -> p.dateTo <= Date() } */
        return procurations.getAllProcurations().filter { p -> p.dateTo <= Date() }
    }
}

data class ClientResponse(val result: Any)

fun main() {
    IoTAPI().start()
    //RuleManager().updateDsl(RuleManager.sampleDsl)
}