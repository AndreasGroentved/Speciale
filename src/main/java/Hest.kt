import AuthManager.Hest5
import DeviceManager.Hest3
import Integrations.Hest8
import IoTAPI.Hest2
import IoTDevices.Hest4
import Tangle.Hest6
import WebApp.Hest7

class Hest {

    init {
        val iotAPI = Hest2()
        val deviceManager = Hest3()
        val tangle = Hest6()
        val webApp = Hest7()
        val iotDevices = Hest4()
        val integrations = Hest8()
        val authManager = Hest5()

    }
}