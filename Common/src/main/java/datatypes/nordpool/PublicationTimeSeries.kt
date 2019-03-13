package datatypes.nordpool

import com.google.gson.annotations.SerializedName
import javax.annotation.processing.Generated

@Generated("com.robohorse.robopojogenerator")
data class PublicationTimeSeries(

    @field:SerializedName("Currency")
    val currency: Currency= Currency(),

    @field:SerializedName("MeasureUnitPrice")
    val measureUnitPrice: MeasureUnitPrice= MeasureUnitPrice(),

    @field:SerializedName("Period")
    val period: Period= Period()

)