package Tangle

fun main() {
    val tangle = TangleController()

    println(
        tangle.attachDeviceToTangle(
            "COOSEED99999999999999999999999999999999999999999999999999999999999999999999999999",
            "asdasd"
        )
    )

    println(tangle.getTransactions("COOSEED99999999999999999999999999999999999999999999999999999999999999999999999999"))

}

