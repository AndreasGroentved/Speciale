package DeviceManager

import io.jsondb.JsonDBTemplate
import org.eclipse.californium.core.CoapClient


class DeviceManager() {


    val devicesIdToIp = mutableMapOf<String, String>()

    init {

        val dbFilesLocation = "db"

        //Java package name where POJO's are present
        val baseScanPackage = "DeviceManager"

        //Optionally a Cipher object if you need Encryption
        //val cipher = DefaultAESCBCCipher("1r8+24pibarAWgS85/Heeg==")

        val jsonDBTemplate = JsonDBTemplate(dbFilesLocation, baseScanPackage)
        val collectionExists = jsonDBTemplate.collectionExists(Device::class.java)

        if (!collectionExists)
            jsonDBTemplate.createCollection<Device>(Device::class.java)

        //jsonDBTemplate.insert<Device>(Device())
        //println(jsonDBTemplate.findAll(Device::class.java))
        Thread {
            ClientDiscovery().startListening {
                println(it)
                val client = CoapClient("$it:5683/temperature")
                val response = client.get()

                if (response != null) {
                    println("response")
                    println(response.responseText)
                } else {
                    println("No response received.")
                }

            }
        }.start()


    }


}

