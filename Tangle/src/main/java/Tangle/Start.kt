package Tangle

fun main() {

    //todo: ordentlig logging rundt omkring
    //todo: fix nogle af alle de skide warnings...
    val tangle = TangleController()
    println(tangle.getNewestBroadcast("EN", "energinetPublicKey"))
    println(tangle.getNewestBroadcast("NP", "nordPoolPublicKey"))

/*
    println(
        tangle.attachDeviceToTangle(
            "COOSEED99999999999999999999999999999999999999999999999999999999999999999999999999",
            Gson().toJson(HeatPump().deviceSpecification)        )
    )

    println(tangle.getTransactions("COOSEED99999999999999999999999999999999999999999999999999999999999999999999999999"))
*/
}

