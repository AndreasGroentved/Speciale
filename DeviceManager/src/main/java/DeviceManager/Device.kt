package DeviceManager

import io.jsondb.annotation.Document
import io.jsondb.annotation.Id

@Document(collection = "Device", schemaVersion = "1.4")
data class Device(@Id var ip: String? = "", var id: String = "")