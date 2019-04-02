package IoTDevices


import IoTDevices.HeatPump.HeatPump

fun main() {
    val heatPump = HeatPump()
    heatPump.start()
    Discovery(heatPump).startDiscovery()
}