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
import org.slf4j.simple.SimpleLoggerFactory
import repositories.*
import spark.*
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


    val requests = mutableMapOf<String, Pair<TangleDeviceSpecification, String>>()


    fun get2(path: String, route: Route) {
        get(path, route, ResponseTransformer { p: Any -> transformer.render(p) })
    }

    fun post2(path: String, route: Route) {
        post(path, route, ResponseTransformer { p: Any -> transformer.render(p) })
    }

    fun put2(path: String, route: Route) {
        put(path, route, ResponseTransformer { p: Any -> transformer.render(p) })
    }

    fun delete2(path: String, route: Route) {
        delete(path, route, ResponseTransformer { p: Any -> transformer.render(p) })
    }


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
        Spark.after("/*") { request, filter ->
            LogI(
                request.requestMethod() + " " + request.uri() + " " + request.body() + " " + request.params().toString()
            )
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

        get2("rule", Route { _: Request, response: Response ->
            ClientResponse(hs.getRules().rule)
        })



        post2("rule", Route { request, _ ->
            val rule = (getParameterMap(request.body())as? Map<String, String>?)?.get("rules")
                ?: return@Route ErrorResponse("invalid json")
            hs.saveRules(rule)
            ruleManager.updateDsl(rule)
        })

        get2("/device", Route { request, response ->
            response.type("application/json")
            val params = request.queryString()
            deviceManger.getDevices(params)
        })

        get2("/device/procurations/pending", Route { _, response ->
            //TODO wrap i ClientResponse
            getActivePendingProcurations(procurations.getAllProcurations())
        })

        put2("/device/procuration/:id/accept", Route { request, response ->
            val id = request.params()[":id"]
            id?.let { pendingProcurations.find { it.messageChainID == id }?.let { respondToProcuration(it, true, seed, tangleController, privateKey) } }?.let { pendingProcurations.removeIf { p -> p.messageChainID == id } }
                ?: ""
        })

        //TODO: REVISIT DE HER, PATH ER LIDT WANK
        put2("/device/procuration/:id/reject", Route { request, response ->
            response.type("application/json")
            val id = request.params()[":id"]
            id?.let { pendingProcurations.find { it.messageChainID == id }?.let { respondToProcuration(it, false, seed, tangleController, privateKey) } }?.let { pendingProcurations.removeIf { p -> p.messageChainID == id } }
                ?: ""
        })

        get2("/device/procurations/accepted", Route { _, response ->
            procurations.getAllProcurations()
        })

        get2("/device/procurations/expired", Route { _, response ->
            getExpiredProcurations()
        })


        get2("/device/:id", Route { request, _ ->
            ClientResponse(deviceManger.getDevice(request!!.params(":id"))!!.specification)
        })


        get2("/device/:id/:path", Route { request, response ->
            val id = request.params(":id")
            val path = request.params(":path")
            val params = if (request.queryParams().isNotEmpty()) "?" + request.queryParams().map { request.params(it) } else ""
            deviceManger.get(PostMessage("this", deviceID = id, path = path + params))
        })

        post2("/device/:id/:path", Route { request, _ ->
            val id = request.params(":id")
            val path = request.params(":path")
            deviceManger.post(PostMessage("this", id, "POST", path, getParameterMap(request.body())))
        })

        get2("/device/:id/time", Route { request, _ ->
            //TODO ????? skal der laves noget her?
            val from = request.queryParams(":from") as String
            val toTime = request.queryParams(":to") as String
            val id = request.params(":id") as String
            val postMessage = PostMessage("this", id, "get", "time", mapOf("from" to from, "to" to toTime))
            deviceManger.post(postMessage)
        })

        delete2("/device/:id", Route { request, _ ->
            val id = request.params(":id")
            deviceManger.unregisterDevice(privateKey, seed, id, tangleController)
        })

        put2("/device/:id", Route { request, _ ->
            val id = request.params(":id")
            deviceManger.registerDevice(privateKey, BigInteger(publicKey.encoded).toString(), id, seed, tangleController)
        })

        post2("/tangle/permissioned/devices", Route { request, _ ->
            val postMessage = request.body().let {
                gson.fromJson(it, PostMessage::class.java)
            } ?: throw RuntimeException("invalid postMessage")
            sendMethodCall(postMessage, privateKey, postMessage.addressTo)
            ClientResponse("success")
        })


        get2("/tangle/permissioned/devices", Route { request: Request, response: Response ->
            tangleController.getBroadcastsUnchecked(Tag.PROACK).mapNotNull {
                val proAck = gson.fromJson(it.first.substringBefore("__"), ProcurationAck::class.java)
                procurationAcks.saveProcuration(proAck)
                val mId = proAck.messageChainID
                requests[mId]
            }.let { ClientResponse(it) }
        })






        get2("/tangle/unpermissioned/devices", Route { _, _ ->
            val map = tangleController.getBroadcastsUnchecked(Tag.DSPEC).map {
                Pair(gson.fromJson(it.first.substringBefore("__"), TangleDeviceSpecification::class.java), it.second)
            }
            ClientResponse(map)
        })


        //TODO: OVERVEJ MESSAGE DESIGN HER + navnet recipientPublicKey + !!
        post2("/tangle/unpermissioned/devices/procuration", Route { request, _ ->
            val params = getParameterMap(request.body())
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
            ).let { ClientResponse("yolo") }
        })



        get2("/tangle/messages/:deviceID", Route { request, _ ->
            request.params("deviceID")?.let { messageRepo.getMessages(it).sortedByDescending { m -> m.timestamp } }
        })

        get2("/tangle/messagechainid/:deviceID", Route { request, _ ->
            request.params("deviceID")?.let { sentProcurations.getProcurationDeviceID(it).messageChainID }
        })

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
        if (verifiedSignature == null || !verifiedSignature) {
            LogE("cannot verify message")
            return
        }
        val result = when (message.postMessage.type.toLowerCase()) {
            "get" -> deviceManger.get(message.postMessage)
            "post" -> deviceManger.post(message.postMessage)
            else -> ErrorResponse("ERROR method type not supported: ${message.postMessage.type}")
        }
        val response = gson.toJson(result)
        val signature = EncryptionHelper.signBase64(privateKey, response)
        tangleController.attachTransactionToTangle(seed, response + "__" + signature, Tag.MR, message.addressFrom)
    }

    private fun sendMethodCall(postMessage: PostMessage, privateKey: PrivateKey, addressTo: String) {
        val toJson = gson.toJson(postMessage)
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

    private fun getParameterMap(body: String): Map<String, String> {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
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

    companion object {
        val transformer = JsonTransformer()


/*
        fun Spark.get(path: String, route: Route): Unit {
            get(path,{
                route.
            })
        }*/
    }
}

class JsonTransformer : ResponseTransformer {

    private val gson = Gson()

    override fun render(model: Any) = gson.toJson(model)


}


fun main() {
    IoTAPI().start()
    //RuleManager().updateDsl(RuleManager.sampleDsl)
}