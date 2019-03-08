package IoTAPI

import java.io.File


fun main(args: Array<String>) {
    println("Hello, world!")

    val f = File("dba.json")
    if (!f.exists()) {
        f.createNewFile()
    } else {
        println("File already exists")
    }



}