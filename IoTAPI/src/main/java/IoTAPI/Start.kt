package IoTAPI

import DeviceManager.DeviceManager
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import helpers.PostMessage
import spark.Filter
import spark.Request
import spark.Response
import spark.Spark.*


fun main(args: Array<String>) {
    val deviceManger = DeviceManager()
    deviceManger.startDiscovery()
    val gson = Gson()

    println("after discovery")

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

    before(Filter { request: Request, response: Response -> response.header("Access-Control-Allow-Origin", "*") })


    get("/devices") { request, response ->
        response.type("application/json")
        deviceManger.getAllDevices()
    }
    get("/device/:id") { request, response -> deviceManger.getDevice(request.params(":id")) }
    get("/device/:id/:path") { request, response ->
        val id = request.params(":id")
        val path = request.params(":path")
        deviceManger.get(id, path)
    }

    post("/device/:id/:path") { request, response ->
        val id = request.params(":id")
        val path = request.params(":path")
        val parameter = request.queryParams("parameter")


        println(getParameterMap(request.body()))
        deviceManger.post(id, path, parameter)
    }

    get("/device/:id/price") { request, response ->
        val from = request.params(":from").toLong()
        val to = request.params(":to").toLong()
        val id = request.params(":id").toString()
        deviceManger.getSavingsForDevice(from, to, id)
    }

    get("/device/:id/time") { request, response ->
        val from = request.queryParams(":from") as String
        val toTime = request.queryParams(":to") as String
        val postMessage = PostMessage(mapOf("from" to from, "to" to toTime))
        val id = request.params(":id") as String
        deviceManger.post(id, "time", gson.toJson(postMessage))
    }

    get("/price") { request, response ->
        val from = request.params(":from").toLong()
        val to = request.params(":to").toLong()
        deviceManger.getAllSavings(from, to)
    }


}

fun getParameterMap(body: String): Map<String, String> {
    val mapType = object : TypeToken<Map<String, String>>() {}.type
    return Gson().fromJson(body, mapType)
}

