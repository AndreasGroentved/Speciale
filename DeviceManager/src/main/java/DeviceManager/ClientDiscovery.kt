package DeviceManager

import org.jgroups.JChannel
import org.jgroups.Message
import org.jgroups.ReceiverAdapter
import org.jgroups.View

class ClientDiscovery : ReceiverAdapter() {


    private var channel = JChannel().setReceiver(this)!!
    private var callback: ((String) -> (Unit))? = null


    fun startListening(idCallBack: (String) -> (Unit)) {
        callback = idCallBack
        channel.connect("DiscoveryCluster")
    }

    override fun viewAccepted(new_view: View) = println("** view: $new_view") //todo: something more exciting and betterer than a println


    override fun receive(msg: Message) {
        callback?.invoke(String(msg.buffer))
    }

}