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
        }
        thread?.start()
    }

    override fun viewAccepted(new_view: View) {
        val device = Device(IdIp(ioTDevice.id, ip), ioTDevice.deviceSpecification)
        val simpleDeviceString = gson.toJson(device)
        channel.send(Message(null, simpleDeviceString.toByteArray()))
    }
}