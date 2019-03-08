package DeviceManager

import org.jgroups.JChannel
import org.jgroups.Message
import org.jgroups.ReceiverAdapter
import org.jgroups.View
import org.jgroups.util.MessageBatch

class ClientDiscovery : ReceiverAdapter() {


    private var channel = JChannel().setReceiver(this)!!
    private var callback: ((String) -> (Unit))? = null

    init {

    }

    fun startListening(idCallBack: (String) -> (Unit)) {
        callback = idCallBack
        channel.connect("DiscoveryCluster")
    }


    override fun receive(batch: MessageBatch) {
        for (msg in batch) {
            try {
                receive(msg)
            } catch (t: Throwable) {
                println(t)
            }
        }
    }

    override fun viewAccepted(new_view: View) {
        println("** view: $new_view")
    }

    override fun receive(msg: Message) {
        callback?.invoke(String(msg.buffer))
    }

}