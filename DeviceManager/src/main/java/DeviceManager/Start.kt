package DeviceManager

import java.lang.Thread.sleep
import java.net.InetAddress


fun main(args: Array<String>) {

    val inetAddress = InetAddress.getLocalHost()
    System.out.println("IP Address:- " + inetAddress.hostAddress);
    val deviceManager = DeviceManager()
    System.out.println("Host Name:- " + inetAddress.hostName)

    Thread {
        deviceManager.broadcast("yolo", InetAddress.getByName("255.255.255.255"))
    }.start()
    sleep(300)
    val a = deviceManager.listAllBroadcastAddresses()
    println(a)

}