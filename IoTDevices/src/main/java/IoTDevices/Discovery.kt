package IoTDevices

import Helpers.SimpleDevice
import com.google.gson.Gson
import org.jgroups.JChannel
import org.jgroups.Message
import org.jgroups.ReceiverAdapter
import org.jgroups.View
import java.net.InetAddress


class Discovery : ReceiverAdapter() {

    private var channel: JChannel = JChannel().setReceiver(this)
    private var running = false
    private var ip = ""
    private val gson = Gson()

    fun startDiscovery() {
        ip = InetAddress.getLocalHost().hostAddress
        channel.connect("DiscoveryCluster")
        Thread {
            running = true
            announceLoop()
        }.start()

    }

    private fun announceLoop() {
        while (running) { //TODO det er nok, nok at at gøre dette i viewAccepted een gang
            Thread.sleep(2000)
            val simpleDeviceString = gson.toJson(SimpleDevice(ip, ip))
            channel.send(Message(null, simpleDeviceString.toByteArray()))
        }
    }

    override fun viewAccepted(new_view: View) {
        println("** view: $new_view")
    }
}