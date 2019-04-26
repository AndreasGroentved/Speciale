package IoTDevices

import com.google.gson.Gson
import datatypes.iotdevices.Device
import datatypes.iotdevices.IdIp
import helpers.LogI
import org.jgroups.JChannel
import org.jgroups.Message
import org.jgroups.ReceiverAdapter
import org.jgroups.View
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException


class Discovery(val ioTDevice: IoTDevice) : ReceiverAdapter() {

    private var channel: JChannel = JChannel().setReceiver(this)
    private var running = false
    private var ip = ""
    private val gson = Gson()
    private var thread: Thread? = null


    fun startDiscovery() {
        //ip = InetAddress.getLocalHost().hostAddress
        ip = getFirstNonLoopbackAddress()?.hostAddress ?: throw RuntimeException("error getting ip address")
        LogI(ip)
        channel.connect("DiscoveryCluster")
        thread = Thread {
            running = true
        }
        thread?.start()
    }


    //https://stackoverflow.com/questions/901755/how-to-get-the-ip-of-the-computer-on-linux-through-java
    @Throws(SocketException::class)
    private fun getFirstNonLoopbackAddress(): InetAddress? {
        val en = NetworkInterface.getNetworkInterfaces()
        while (en.hasMoreElements()) {
            val i = en.nextElement() as NetworkInterface
            val en2 = i.inetAddresses
            while (en2.hasMoreElements()) {
                val addr = en2.nextElement() as InetAddress
                if (!addr.isLoopbackAddress && addr is Inet4Address) {
                    return addr
                }
            }
        }
        return null
    }

    override fun viewAccepted(new_view: View) {
        val device = Device(IdIp(ioTDevice.id, ip), ioTDevice.deviceSpecification)
        val simpleDeviceString = gson.toJson(device)
        channel.send(Message(null, simpleDeviceString.toByteArray()))
    }
}