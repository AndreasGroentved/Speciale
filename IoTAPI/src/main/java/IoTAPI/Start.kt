package IoTAPI

import DeviceManager.DeviceManager
import Tangle.TangleController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import datatypes.ClientResponse
import datatypes.ErrorResponse
import datatypes.iotdevices.*
import datatypes.tangle.Tag
import helpers.*
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import repositories.*
import spark.*
import spark.Spark.*
import java.math.BigInteger
import java.security.PrivateKey
import java.security.PublicKey
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class IoTAPI {
    private val hs = HouseRules()
    private val seed = "TEEEA9999999999999999999999999999999999999999999999999999999999999999999999999999"
    private val deviceManger = DeviceManager()
    private val gson = Gson()
    private val procurations = AcceptedProcurations()
    private val pendingProcurations: MutableList<Pair<Procuration, String>> = mutableListOf()
    private val tangleController = TangleController(seed)
    private val threadPool = ScheduledThreadPoolExecutor(1)
    private val pendingMethodCalls = CopyOnWriteArrayList<PostMessageHack>()
    private lateinit var privateKey: PrivateKey
    private lateinit var publicKey: PublicKey
    private val messageRepo = MessageRepo()
    private val procurationAcks = ProcurationAcks()
    private val sentProcurations = SentProcurations()
    private val deviceSpecifications = TangleDeviceSpecifications()
    private val ws = WebSocketHandler()


    val tangleDeviceCallback: (postMessage: PostMessage, result: (datatypes.Response) -> (Unit)) -> (Unit) = { pM: PostMessage, result ->
        val tdsa = deviceSpecifications.getAllPermissionedSpecs(listOf(pM.deviceID)).firstOrNull()
        val proc = sentProcurations.getProcurationDeviceID(pM.deviceID)

        proc?.let { (messageChainID) -> tdsa?.let { PostMessage(messageChainID, pM.deviceID, pM.type, pM.path, pM.params, it.address) } } //TODO timeout
            ?.apply {
                sendMethodCall(this, privateKey, this.addressTo)
            }
            ?.apply {
                val listener = MessageRepo.ListenerStuff(deviceID, path)
                val callback = { resp: datatypes.Response ->
                    messageRepo.deRegister(listener)
                    result(resp)
                }
                listener.callback = callback
                messageRepo.registerSubscription(listener)
            } ?: result(ErrorResponse("Invalid device id"))
    }

    private val ruleManager = RuleManager(deviceManger, tangleDeviceCallback, tangleController = tangleController)


    private val transformer = JsonTransformer()
    private val lambda = ResponseTransformer { transformer.render(it) }

    fun get(path: String, route: Route) {
        get(path, route, lambda)
    }

    fun post(path: String, route: Route) {
        post(path, route, lambda)
    }

    fun put(path: String, route: Route) {
        put(path, route, lambda)
    }

    fun delete(path: String, route: Route) {
        delete(path, route, lambda)
    }


    fun start() {
        deviceManger.startDiscovery()
        threadPool.scheduleAtFixedRate({ methodTask() }, 0, 5, TimeUnit.SECONDS)
        threadPool.scheduleAtFixedRate({ methodResponseTask() }, 0, 5, TimeUnit.SECONDS)
        threadPool.scheduleAtFixedRate({ StatisticsCollector.printStats(null) }, 0, 2, TimeUnit.MINUTES)
        port(PropertiesLoader.instance.getProperty("iotApiPort").toInt())

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

        messageRepo.registerUpdates {
            LogI("sending update")
            ws.sendUpdate(it)
        }

        webSocket("/messageChannel", ws)
        init()


        exception(Exception::class.java) { e, _, _ -> LogE(e) }
        after("/*") { request, _ ->
            LogI(request.requestMethod() + " " + request.uri() + " " + request.body() + " " + request.params().toString())
        }

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

        get("rule", Route { _: Request, _: Response ->
            ClientResponse(hs.getRules().rule)
        })


        post("rule", Route { request, _ ->
            val rule = (getParameterMap(request.body())).get("rules")
                ?: return@Route ErrorResponse("invalid json")
            hs.saveRules(rule)
            ruleManager.updateDsl(rule)
        })

        get("/device", Route { request, _ ->
            val params = request.queryString()
            deviceManger.getDevices(params)
        })

        get("/device/procurations/sent/accepted", Route { _, _ ->
            val allSentProcurations = sentProcurations.getAllSentProcurations()
            ClientResponse(allSentProcurations.filter { sent -> procurationAcks.getAllAcceptedProAck().firstOrNull { it.messageChainID == sent.messageChainID } != null })
        })

        get("/device/procurations/sent/pending", Route { _, _ ->
            val allSentProcurations = sentProcurations.getAllSentProcurations()
            ClientResponse(allSentProcurations.filter { sent -> procurationAcks.getAllProAck().firstOrNull { it.messageChainID == sent.messageChainID } == null })
        })

        get("/device/procurations/sent/expired", Route { _, _ ->
            ClientResponse(sentProcurations.getExpiredProcurations())
        })

        get("/device/procurations/received/pending", Route { _, _ ->
            ClientResponse(getActivePendingProcurations(procurations.getAllProcurations()))
        })

        put("/device/procuration/received/:id/accept", Route { request, _ ->
            val id = request.params()[":id"]
            id?.let { pendingProcurations.find { it.first.messageChainID == id }?.let { respondToProcuration(it.first, true, it.second) } }?.let { pendingProcurations.removeIf { p -> p.first.messageChainID == id } }
                ?: ""
        })

        put("/device/procuration/received/:id/reject", Route { request, response ->
            response.type("application/json")
            val id = request.params()[":id"]
            id?.let { pendingProcurations.find { it.first.messageChainID == id }?.let { respondToProcuration(it.first, false, it.second) } }?.let { pendingProcurations.removeIf { p -> p.first.messageChainID == id } }
                ?: ""
        })

        get("/device/procurations/received/accepted", Route { _, _ ->
            ClientResponse(procurations.getAcceptedProcurations())
        })

        get("/device/procurations/received/expired", Route { _, _ ->
            ClientResponse(procurations.getExpiredProcurations())
        })


        get("/device/:id", Route { request, _ ->
            ClientResponse(deviceManger.getDevice(request!!.params(":id"))!!.specification)
        })


        get("/device/:id/:path", Route { request, _ ->
            val id = request.params(":id")
            val path = request.params(":path")
            val params = if (request.queryParams().isNotEmpty()) "?" + request.queryParams().map { request.params(it) } else ""
            deviceManger.get(PostMessage("this", deviceID = id, path = path + params))
        })

        post("/device/:id/:path", Route { request, _ ->
            val id = request.params(":id")
            val path = request.params(":path")
            deviceManger.post(PostMessage("this", id, "POST", path, getParameterMap(request.body())))
        })

        get("/time/:id", Route { request, _ ->
            val from = request.queryParams("from") as String
            val toTime = request.queryParams("to") as String
            val id = request.params(":id") as String
            val postMessage = PostMessage("this", id, "get", "time", mapOf("from" to from, "to" to toTime))
            deviceManger.get(postMessage)
        })

        delete("/device/:id", Route { request, _ ->
            val id = request.params(":id")
            deviceManger.unregisterDevice(privateKey, BigInteger(publicKey.encoded).toString(), seed, id, tangleController)
        })

        put("/device/:id", Route { request, _ ->
            val id = request.params(":id")
            deviceManger.registerDevice(privateKey, BigInteger(publicKey.encoded).toString(), id, seed, tangleController)
        })

        post("/tangle/permissioned/devices", Route { request, _ ->
            val postMessage = request.body().let {
                gson.fromJson(it, PostMessage::class.java)
            } ?: throw RuntimeException("invalid postMessage")
            sendMethodCall(postMessage, privateKey, postMessage.addressTo)
            ClientResponse("success")
        })


        fun getPermissionedDevices(): ClientResponse {
            tangleController.getMessagesUnchecked(Tag.PROACK).mapNotNull {
                val proAckString = it.first.substringBefore("__")
                val proAck = gson.fromJson(proAckString, ProcurationAck::class.java)
                val signature = it.first.substringAfter("__")
                sentProcurations.getProcurationMessageChainID(proAck.messageChainID)?.let { d ->
                    deviceSpecifications.getUnpermissionedSpec(d.deviceID)?.let { spec ->
                        if (EncryptionHelper.verifySignatureBase64(EncryptionHelper.loadPublicECKeyFromBigInteger(BigInteger(spec.tangleDeviceSpecification.publicKey)), proAckString, signature)) procurationAcks.saveProAck(proAck)
                    }
                }
            }
            procurationAcks.removeProAcks(procurations.getExpiredProcurationsLessThan7DaysOld())
            val procs = procurationAcks.getAllAcceptedProAck().mapNotNull { sentProcurations.getProcurationMessageChainID(it.messageChainID)?.deviceID }
            return ClientResponse(deviceSpecifications.getAllPermissionedSpecs(procs))
        }

        get("/tangle/permissioned/devices", Route { _, _ ->
            getPermissionedDevices()
        })


        get("/tangle/unpermissioned/devices", Route { _, _ ->
            val registered = tangleController.getBroadcastsUnchecked(Tag.DSPEC).mapNotNull {
                TDSA(gson.fromJson(it.first.substringBefore("__"), TangleDeviceSpecification::class.java), it.second)
            }
            registered.forEach { deviceSpecifications.saveTDSA(it) }
            val unregistered = tangleController.getBroadcastsUnchecked(Tag.XDSPEC).mapNotNull {
                Triple(gson.fromJson(it.first.substringBefore("__"), TangleDeviceSpecification::class.java), it.first.substringBefore("__"), it.first.substringAfter("__"))
            }
            unregistered.forEach { sentProcurations.getProcurationDeviceID(it.first.deviceSpecification.id)?.let { proc -> procurationAcks.removeProAcks(listOf(proc)) } }
            val filtered = deviceSpecifications.getAllSpecs().filter { r ->
                unregistered.filter { u ->
                    r.tangleDeviceSpecification.deviceSpecification.id == u.first.deviceSpecification.id && r.tangleDeviceSpecification.publicKey == u.first.publicKey
                }.any {
                    EncryptionHelper.verifySignatureBase64(
                        EncryptionHelper.loadPublicECKeyFromBigInteger(BigInteger(r.tangleDeviceSpecification.publicKey)), it.second, it.third
                    )
                }
            }
            filtered.forEach { deviceSpecifications.removeTDSA(it) }
            ClientResponse(deviceSpecifications.getAllSpecs())
        })

        post("/tangle/unpermissioned/devices/procuration", Route { request, _ ->
            val params = getParameterMap(request.body())
            val dateFrom = params["dateFrom"]?.toLongOrNull() ?: throw RuntimeException("Invalid from date")
            val dateTo = params["dateTo"]?.toLongOrNull() ?: throw RuntimeException("Invalid to date")
            val messageId = UUID.randomUUID().toString()
            requestProcuration(
                Procuration(
                    messageId, params.getValue("deviceId"), BigInteger(publicKey.encoded),
                    Date(dateFrom), Date(dateTo)
                ), params.getValue("addressTo"), tangleController, privateKey
            )
            ClientResponse("success")
        })


        get("/tangle/messages/:deviceID", Route { request, _ ->
            request.params("deviceID")?.let { ClientResponse(messageRepo.getMessages(it).sortedByDescending { m -> m.timestamp }) }
        })

        get("/tangle/messagechainid/:deviceID", Route { request, _ ->
            request.params("deviceID")?.let { ClientResponse(sentProcurations.getProcurationDeviceID(it)!!.messageChainID) }
        })
    }


    @WebSocket
    inner class WebSocketHandler {
        private var user: Session? = null
        private val gson = Gson()


        fun sendUpdate(obj: Any) {
            user?.remote?.sendString(gson.toJson(obj))
        }

        @OnWebSocketConnect
        @Throws(Exception::class)
        fun onConnect(user: Session) {
            this.user = user
        }

        @OnWebSocketClose
        fun onClose(user: Session, statusCode: Int, reason: String) {
            this.user = null
        }

        @OnWebSocketMessage
        fun onMessage(user: Session, message: String) {
        }
    }


    private fun methodTask() {
        pendingMethodCalls += tangleController.getPendingMethodCalls(procurations.getAcceptedProcurations())
        LogI(pendingMethodCalls)
        pendingMethodCalls.forEach { handleMethodType(it) }
        pendingMethodCalls.clear()
    }


    private fun methodResponseTask() {
        val messagesUnchecked = tangleController.getMessagesUnchecked(Tag.MR)
        messagesUnchecked.forEach {
            val message = gson.fromJson(it.first.substringBefore("__"), Message::class.java)
            val procuration = sentProcurations.getProcurationDeviceID(message.deviceID) ?: return@forEach
            val validSignature = EncryptionHelper.verifySignatureBase64(EncryptionHelper.loadPublicECKeyFromBigInteger(procuration.recipientPublicKey), it.first.substringBefore("__"), it.first.substringAfter("__"))
            if (validSignature) {
                messageRepo.saveMessage(message)
            }
        }
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
        if (verifiedSignature == null || !verifiedSignature) {
            LogW("cannot verify message")
            return
        }
        val result = when (message.postMessage.type.toLowerCase()) {
            "get" -> deviceManger.get(message.postMessage)
            "post" -> deviceManger.post(message.postMessage)
            else -> ErrorResponse("ERROR method type not supported: ${message.postMessage.type}")
        }
        val response = gson.toJson(Message(gson.toJson(result), Date(), message.postMessage.deviceID, message.postMessage.messageChainID, message.postMessage.path))
        val signature = EncryptionHelper.signBase64(privateKey, response)
        tangleController.attachTransactionToTangle(response + "__" + signature, Tag.MR, message.addressFrom)
    }


    private fun sendMethodCall(postMessage: PostMessage, privateKey: PrivateKey, addressTo: String) {
        val toJson = gson.toJson(postMessage)
        val signBase64 = EncryptionHelper.signBase64(privateKey, toJson)
        tangleController.attachTransactionToTangle(toJson + "__" + signBase64, Tag.MC, addressTo)
        messageRepo.saveMessage(Message(toJson, Date(), postMessage.deviceID))
    }

    private fun respondToProcuration(procuration: Procuration, accepted: Boolean, addressTo: String) {
        val procurationAck = ProcurationAck(procuration.messageChainID, accepted)
        val json = gson.toJson(procurationAck)
        val signBase64 = EncryptionHelper.signBase64(privateKey, json)
        tangleController.attachTransactionToTangle(json + "__" + signBase64, Tag.PROACK, addressTo)?.let {
            procurations.saveProcuration(procuration)
            messageRepo.saveMessage(Message(json, Date(), procuration.deviceID))
        }
    }

    private fun requestProcuration(procuration: Procuration, addressTo: String, tangle: TangleController, privateKey: PrivateKey) {
        val json = gson.toJson(procuration)
        val signBase64 = EncryptionHelper.signBase64(privateKey, json)
        tangle.attachTransactionToTangle(json + "__" + signBase64, Tag.PRO, addressTo)?.let { messageRepo.saveMessage(Message(json, Date(), procuration.deviceID)) }
        sentProcurations.saveProcuration(procuration)
    }

    private fun getParameterMap(body: String): Map<String, String> {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(body, mapType)
    }

    private fun getActivePendingProcurations(accepted: List<Procuration>): List<Procuration> {
        val messages = tangleController.getMessagesUnchecked(Tag.PRO)
        val procurations = messages.mapNotNull { (first, second) ->
            try {
                Pair(gson.fromJson(first.substringBefore("__"), Procuration::class.java), second)
            } catch (e: Exception) {
                null
            }
        }
        pendingProcurations.addAll(procurations.filter { p -> accepted.firstOrNull { a -> p.first.messageChainID == a.messageChainID } == null })
        return pendingProcurations.map { it.first }
    }
}

class JsonTransformer : ResponseTransformer {
    private val gson = Gson()
    override fun render(model: Any) = gson.toJson(model)
}


fun main() {
    IoTAPI().start()
}