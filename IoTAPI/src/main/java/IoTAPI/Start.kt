package IoTAPI

import DeviceManager.DeviceManager
import spark.Spark.get
import spark.Spark.post


fun main(args: Array<String>) {
    val deviceManger = DeviceManager()
    deviceManger.startDiscovery()

    get("/devices") { _, _ -> deviceManger.getAllDevices() }
    get("/device/:id") { request, _ -> deviceManger.getDevice(request.params(":id")) }
    get("/device/:id/:path") { request, response ->
        val id = request.params(":id")
        val path = request.params(":path")
        deviceManger.get(id, path)
    }

    post("/device/:id/:path") { request, response ->
        val id = request.params(":id")
        val path = request.params(":path")
        val parameter = request.params(":parameter")
        deviceManger.post(id, path, parameter)
    }

    get("/price/device/:id") { request, response ->
        val from = request.params(":from").toLong()
        val to = request.params("to").toLong()
        val id = request.params("id")
        deviceManger.getSavingsForDevice(from, to, id)
    }

    get("/price") { request, response ->
        val from = request.params(":from").toLong()
        val to = request.params("to").toLong()
        deviceManger.getAllSavings(from, to)
    }


}

