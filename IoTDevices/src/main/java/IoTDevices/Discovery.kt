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
    private var ip = ""
    private val gson = Gson()


    fun startDiscovery() {
        ip = InetAddress.getLocalHost().hostAddress
        channel.connect("DiscoveryCluster")
    }

    override fun viewAccepted(new_view: View) {
        val device = Device(IdIp(ioTDevice.id, ip), ioTDevice.deviceSpecification)
        val simpleDeviceString = gson.toJson(device)
        channel.send(Message(null, simpleDeviceString.toByteArray()))
    }
}