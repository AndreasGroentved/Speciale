package IoTAPI

import DeviceManager.DeviceManager
import spark.kotlin.ignite


fun main(args: Array<String>) {
    val deviceManger = DeviceManager()
    deviceManger.startDiscovery()

    val http = ignite().run {
        get("/devices") { deviceManger.getAllDevices() }
        get("/device/:id") { deviceManger.getDevice(request.params(":id")) }
        get("/device/:id/:path") {
            val id = request.params(":id")
            val path = request.params(":path")
            deviceManger.get(id, path)
        }

        post("/device/:id/:path") {
            val id = request.params(":id")
            val path = request.params(":path")
            val parameter = request.params(":parameter")
            deviceManger.post(id, path, parameter)
        }

        get("/price/device/:id") {
            val from = request.params(":from").toLong()
            val to = request.params(":to").toLong()
            val id = request.params(":id")
            deviceManger.getSavingsForDevice(from, to, id)
        }

        get("/price") {
            val from = request.params(":from").toLong()
            val to = request.params(":to").toLong()
            deviceManger.getAllSavings(from, to)
        }
    }


}

