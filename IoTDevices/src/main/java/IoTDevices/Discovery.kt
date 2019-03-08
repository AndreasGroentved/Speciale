package IoTDevices

import org.jgroups.JChannel
import org.jgroups.Message
import org.jgroups.ReceiverAdapter
import org.jgroups.View
import java.net.InetAddress


class Discovery : ReceiverAdapter() {

    private var channel: JChannel? = null
    private var running = false
    private var id = ""

    fun startDiscovery() {
        id = InetAddress.getLocalHost().hostAddress
        channel = JChannel().setReceiver(this)
        channel?.connect("DiscoveryCluster")

        Thread {
            running = true
            announceLoop()
        }.start()

    }

    private fun announceLoop() {
        while (running) {
            Thread.sleep(2000)
            channel?.send(Message(null, id.toByteArray()))
        }
    }

    override fun viewAccepted(new_view: View) {
        println("** view: $new_view")
    }
}