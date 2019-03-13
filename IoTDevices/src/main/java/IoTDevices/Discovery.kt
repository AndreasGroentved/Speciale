package IoTDevices

import com.google.gson.Gson
import datatypes.iotdevices.Device
import datatypes.iotdevices.IdIp
import org.jgroups.JChannel
import org.jgroups.Message
import org.jgroups.ReceiverAdapter
import org.jgroups.View
import java.net.InetAddress


class Discovery(val ioTDevice: IoTDevice) : ReceiverAdapter() {

    private var channel: JChannel = JChannel().setReceiver(this)
    private var running = false
    private var ip = ""
    private val gson = Gson()
    private var thread: Thread? = null


    fun startDiscovery() {
        ip = InetAddress.getLocalHost().hostAddress
        channel.connect("DiscoveryCluster")
        thread = Thread {
            running = true
            announceLoop()
        }
        thread?.start()
    }


    fun stopDiscovery() {
        thread?.stop()
    }

    private fun announceLoop() {
        while (thread?.isAlive == true) { //TODO det er nok, nok at at gøre dette i viewAccepted een gang
            Thread.sleep(2000)
            if (!thread?.isAlive!!) break
            val simpleDeviceString = gson.toJson(Device(IdIp(ioTDevice.id, ip), gson.toJson(ioTDevice.deviceSpecification))) //TODO hax hax hax
            channel.send(Message(null, simpleDeviceString.toByteArray()))
        }
    }

    override fun viewAccepted(new_view: View) {
        println("** view: $new_view")
    }
}