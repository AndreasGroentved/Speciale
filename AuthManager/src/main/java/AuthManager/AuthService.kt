package AuthManager

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import datatypes.authmanager.User
import datatypes.iotdevices.DeviceSpecification
import spark.Filter
import spark.Request
import spark.Response
import spark.Spark
import spark.Spark.*
import java.io.PrintWriter
import java.io.StringWriter
import java.math.BigInteger
import java.util.*


class AuthService {

    private val tokensToUsers = mutableMapOf<String, User>()
    private val users = mutableListOf<User>()
    private val gson = Gson()

    fun startService() {
        Spark.exception(
            Exception::class.java
        ) { e, _, _ ->
            val sw = StringWriter()
            val pw = PrintWriter(sw, true)
            e.printStackTrace(pw)
            System.err.println(sw.buffer.toString())
        }
        Spark.after("/*") { request, _ -> println(request.pathInfo());println(request.body()); println(tokensToUsers); println(users) }

        options(
            "/*"
        )
        { request, response ->

            val accessControlRequestHeaders = request
                .headers("Access-Control-Request-Headers")
            if (accessControlRequestHeaders != null) {
                response.header(
                    "Access-Control-Allow-Headers",
                    accessControlRequestHeaders
                )
            }
            val accessControlRequestMethod = request
                .headers("Access-Control-Request-Method")
            if (accessControlRequestMethod != null) {
                response.header(
                    "Access-Control-Allow-Methods",
                    accessControlRequestMethod
                )
            }

            "OK"
        }

        before(Filter
        { _: Request, response: Response -> response.header("Access-Control-Allow-Origin", "*") })


        post("/register/user")
        { request, response ->
            val body = getParameterMap(request.body())
            users.add(User(body.getValue("username"), body.getValue("password"), BigInteger(body["key"]), mutableListOf()))
            response.status(200)
            "success"
        }
        post("/login")
        { request, response ->
            val body = getParameterMap(request.body())
            val user = users.find { (username, password) -> username == body["username"] && password == body["password"] }
            val token = UUID.randomUUID().toString()
            tokensToUsers[token] = user!!
            response.status(200)
            token

        }
        post("/register/device")
        { request, response ->
            val token = getParameterMap(request.body())["token"]
            val deviceSpecificationJson = getParameterMap(request.body())["deviceSpecification"]
            tokensToUsers[token]!!.devices.add(gson.fromJson(deviceSpecificationJson, DeviceSpecification::class.java))
            response.status(200)
            deviceSpecificationJson
        }
        post("/deregister/device")
        { request, response ->
            val token = getParameterMap(request.body())["token"]
            val deviceSpecificationJson = getParameterMap(request.body())["deviceSpecification"]
            tokensToUsers[token]!!.devices.remove(gson.fromJson(deviceSpecificationJson, DeviceSpecification::class.java))
            response.status(200)
            deviceSpecificationJson
        }
    }

    private fun getParameterMap(body: String): Map<String, String> {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return Gson().fromJson(body, mapType)
    }
}
