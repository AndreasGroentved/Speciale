package datatypes.energinet

import com.google.gson.annotations.SerializedName

data class RecordsItem(
    @field:SerializedName("PriceArea")
    val priceArea: String? = null,
    @field:SerializedName("Minutes5DK")
    val minutes5DK: String? = null,
    @field:SerializedName("Minutes5UTC")
    val minutes5UTC: String? = null,
    @field:SerializedName("CO2Emission")
    val cO2Emission: Int? = null
)
