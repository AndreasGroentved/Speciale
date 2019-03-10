package datatypes.nordpool

import com.google.gson.annotations.SerializedName
import javax.annotation.processing.Generated

@Generated("com.robohorse.robopojogenerator")
data class PublicationTimeSeries(

    var signature: String? = null,

    @field:SerializedName("Currency")
    val currency: Currency? = null,

    @field:SerializedName("MeasureUnitPrice")
    val measureUnitPrice: MeasureUnitPrice? = null,

    @field:SerializedName("Period")
    val period: Period? = null

)