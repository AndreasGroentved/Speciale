package datatypes

sealed class Response

data class ClientResponse(val result: Any) : Response()
data class ErrorResponse(val error: String) : Response()