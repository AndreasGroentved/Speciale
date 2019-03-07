package IoTDevices

import com.google.gson.Gson
import org.eclipse.californium.core.server.resources.Resource

class DeviceSpecification(val id: String, val resources: Collection<Resource>) {
    fun toJson(): String {
        return Gson().toJson(this)
    }
}