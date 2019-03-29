package datatypes.iotdevices

data class ResponseWithDeviceID(val reponseToClient: ResponseToClient = ResponseToClient(""), val deviceID: String = "")