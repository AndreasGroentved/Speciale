package DeviceManager

import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*


class DeviceManager {
    private var socket: DatagramSocket? = null

    @Throws(IOException::class)
    fun broadcast(broadcastMessage: String, address: InetAddress) {
        socket = DatagramSocket()
        socket?.broadcast = true

        val buffer = broadcastMessage.toByteArray()

        val packet = DatagramPacket(buffer, buffer.size, address, 4445)
        socket?.send(packet)
        socket?.close()
    }


    fun listAllBroadcastAddresses(): List<InetAddress> {
        val broadcastList = ArrayList<InetAddress>()
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()

            if (networkInterface.isLoopback || !networkInterface.isUp) {
                continue
            }

            networkInterface.interfaceAddresses.stream().forEach {
                println(it)
                println(it.address)
            }

            networkInterface.interfaceAddresses.stream()
                .map { it.broadcast }
                .filter { Objects.nonNull(it) }
                .forEach {
                    println(it.address)
                    println(it.canonicalHostName)
                    println(it.hostAddress)
                    println(it.hostName)
                    println(it)
                    broadcastList.add(it)
                }
        }
        return broadcastList
    }


}

