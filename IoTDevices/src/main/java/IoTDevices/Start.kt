package IoTDevices


import IoTDevices.HeatPump.HeatPump

fun main(args: Array<String>) {

    HeatPump().start()
    Discovery().startDiscovery()


    /* val client = CoapClient("10.126.49.76:5683/temperature")
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
     }*/


}