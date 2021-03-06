package AuthManager

import IoTDevices.HeatPump.HeatPump
import com.google.gson.Gson
import datatypes.iotdevices.DeviceSpecification
import helpers.PropertiesLoader
import java.net.HttpURLConnection
import java.net.URL

class AuthServiceClient {
    private val gson = Gson()
    private val address = PropertiesLoader.instance.getProperty("authServiceAddress") + PropertiesLoader.instance.getProperty("authPort")

    fun registerUser(username: String, password: String, publicKey: String): String {
        val url = URL("$address/registerUpdates/user")
        val connection = url.openConnection() as HttpURLConnection
        val params = mapOf(Pair("username", username), Pair("password", password), Pair("publicKey", publicKey))
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        connection.outputStream.write(gson.toJson(params).toByteArray())
        return connection.inputStream.bufferedReader().readText()
    }

    fun login(username: String, password: String): String {
        val url = URL("$address/login")
        val connection = url.openConnection() as HttpURLConnection
        val params = mapOf(Pair("username", username), Pair("password", password))
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        connection.outputStream.write(gson.toJson(params).toByteArray())
        return connection.inputStream.bufferedReader().readText()
    }

    fun registerDevice(token: String, deviceSpecification: DeviceSpecification): String {
        val url = URL("$address/registerUpdates/device")
        val connection = url.openConnection() as HttpURLConnection
        val params = mapOf(Pair("token", token), Pair("deviceSpecification", gson.toJson(deviceSpecification)))
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        connection.outputStream.write(gson.toJson(params).toByteArray())
        return connection.inputStream.bufferedReader().readText()
    }

    fun deRegisterDevice(token: String, deviceSpecification: DeviceSpecification): String {
        val url = URL("$address/deregister/device")
        val connection = url.openConnection() as HttpURLConnection
        val params = mapOf(Pair("token", token), Pair("deviceSpecification", gson.toJson(deviceSpecification)))
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        connection.outputStream.write(gson.toJson(params).toByteArray())
        return connection.inputStream.bufferedReader().readText()
    }
}

fun main() {
    val client = AuthServiceClient()
    println(client.registerUser("hest", "hest", "123"))
    var token = client.login("hest", "hest")
    println(token)
    println(client.registerDevice(token, HeatPump().deviceSpecification))
    token = client.login("hest", "hest")
    println(token)
    println(client.deRegisterDevice(token, HeatPump().deviceSpecification))
}