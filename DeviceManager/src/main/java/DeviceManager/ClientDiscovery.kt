package DeviceManager

import org.jgroups.JChannel
import org.jgroups.Message
import org.jgroups.ReceiverAdapter
import org.jgroups.View
import org.slf4j.simple.SimpleLoggerFactory

class ClientDiscovery : ReceiverAdapter() {


    private var channel = JChannel().setReceiver(this)!!
    private var callback: ((String) -> (Unit))? = null
    private val logger = SimpleLoggerFactory().getLogger("ClientDiscovery")


    fun startListening(idCallBack: (String) -> (Unit)) {
        callback = idCallBack
        channel.connect("DiscoveryCluster")
    }

    override fun viewAccepted(new_view: View) = logger.info("accepted view: $new_view") //todo: skal der ske mere end logging?


    override fun receive(msg: Message) {
        callback?.invoke(String(msg.buffer))
    }

}