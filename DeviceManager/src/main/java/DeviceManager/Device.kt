package DeviceManager

import io.jsondb.annotation.Document
import io.jsondb.annotation.Id

data class Device(var ip: String? = "", var id: String = "")