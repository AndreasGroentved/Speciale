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

    override fun viewAccepted(new_view: View) {
        println("yooo")
        members.map { it }.forEach {
            if (!new_view.containsMember(it.key)) {
                members.remove(it.key)
                removeCallBack?.invoke(it.value)
            }
        }
        println(members)
    }

    override fun receive(msg: Message) {
        members[msg.src] = String(msg.buffer)
        idCallBack?.invoke(String(msg.buffer))
    }

}