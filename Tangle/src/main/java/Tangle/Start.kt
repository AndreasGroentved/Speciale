package Tangle

fun main() {

    val tangle = TangleController()
    println(tangle.getNewestBroadcast("NP"))
/*
    println(
        tangle.attachDeviceToTangle(
            "COOSEED99999999999999999999999999999999999999999999999999999999999999999999999999",
            Gson().toJson(HeatPump().deviceSpecification)        )
    )

    println(tangle.getTransactions("COOSEED99999999999999999999999999999999999999999999999999999999999999999999999999"))
*/
}

