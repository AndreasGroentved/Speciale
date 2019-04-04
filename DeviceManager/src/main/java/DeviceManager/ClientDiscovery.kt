package DeviceManager

import org.jgroups.*

class ClientDiscovery : ReceiverAdapter() {


    private var channel = JChannel().setReceiver(this)!!
    private var idCallBack: ((String) -> (Unit))? = null
    private var removeCallBack: ((String) -> (Unit))? = null
    private val members: MutableMap<Address, String> = mutableMapOf()


    fun startListening(idCallBack: (String) -> (Unit), removeCallBack: (String) -> (Unit)) {
        this.idCallBack = idCallBack
        this.removeCallBack = removeCallBack
        channel.connect("DiscoveryCluster")
    }

    //TODO: ikke sindsygt related, men cancel procurations, når der kommer en XDSPEC message i Tangle
    override fun viewAccepted(new_view: View) {
        members.forEach {
            if (!new_view.containsMember(it.key)) {
                removeCallBack?.invoke(it.value)
            }
        }
    }

    override fun receive(msg: Message) {
        members[msg.src] = String(msg.buffer)
        idCallBack?.invoke(String(msg.buffer))
    }

}