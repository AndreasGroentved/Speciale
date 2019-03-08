package Tangle

import IoTDevices.HeatPump.HeatPump
import com.google.gson.Gson

fun main() {

    val tangle = TangleController()
    println(Gson().toJson(HeatPump().deviceSpecification))
/*
    println(
        tangle.attachDeviceToTangle(
            "COOSEED99999999999999999999999999999999999999999999999999999999999999999999999999",
            Gson().toJson(HeatPump().deviceSpecification)        )
    )

    println(tangle.getTransactions("COOSEED99999999999999999999999999999999999999999999999999999999999999999999999999"))
*/
}

