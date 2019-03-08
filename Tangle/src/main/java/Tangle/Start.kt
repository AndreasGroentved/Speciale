package Tangle

fun main() {

    val tangle = TangleController()

    println(
        tangle.attachDeviceToTangle(
            "COOSEED99999999999999999999999999999999999999999999999999999999999999999999999999",
            ""
        )
    )

    println(tangle.getTransactions("COOSEED99999999999999999999999999999999999999999999999999999999999999999999999999"))

}

