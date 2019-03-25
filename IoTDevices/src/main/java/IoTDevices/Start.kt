package IoTDevices


import IoTDevices.HeatPump.HeatPump

fun main() {

    val heatPump = HeatPump()
    heatPump.start()
    Discovery(heatPump).startDiscovery()


    /* val client = CoapClient("10.126.49.76:5683/temperature")
     val discover = client.discover()

     println(discover)
     val result = client.get()

     if (result != null) {
         println(result.responseText)
     } else {
         println("No result received.")
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