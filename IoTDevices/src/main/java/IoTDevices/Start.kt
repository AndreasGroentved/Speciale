package IoTDevices


import IoTDevices.HeatPump.HeatPump

fun main() {
    val heatPump = HeatPump("hest2")
    heatPump.start()
    Discovery(heatPump).startDiscovery()
}