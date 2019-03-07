package IoTDevices


import com.google.gson.Gson
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.coap.MediaTypeRegistry

fun main(args: Array<String>) {

    val client = CoapClient("10.126.49.76:5683/temperature")

    val discover = client.discover()

    println(discover)
    val response = client.get()

    if (response != null) {
        println(response.responseText)
    } else {
        println("No response received.")
    }

    val params = listOf("2")
    val message = PostMessage(params)
    val json = Gson().toJson(message)
    val post = client.post(json, MediaTypeRegistry.APPLICATION_JSON)

    if(post != null) {
        println(post.responseText)
    } else {
        println("Post not work oh no")
    }

}