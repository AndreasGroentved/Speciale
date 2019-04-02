package datatypes

import java.io.Serializable

data class TimePair(val hour: Long = -1L, val amount: Long = -1L) : Serializable