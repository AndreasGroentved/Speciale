package IoTAPI

import io.jsondb.annotation.Document
import io.jsondb.annotation.Id


@Document(collection = "Hest4", schemaVersion = "1.4")
data class Hest2(@Id var id: String? = null, var message: String? = null)