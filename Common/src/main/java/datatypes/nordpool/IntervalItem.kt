package datatypes.nordpool

import com.google.gson.annotations.SerializedName
import javax.annotation.processing.Generated

@Generated("com.robohorse.robopojogenerator")
data class IntervalItem(

    @field:SerializedName("Pos")
    val pos: Pos? = null,

    @field:SerializedName("Price")
    val price: Price? = null
)