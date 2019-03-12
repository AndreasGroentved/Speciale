package datatypes.nordpool

import com.google.gson.annotations.SerializedName
import javax.annotation.processing.Generated

@Generated("com.robohorse.robopojogenerator")
data class IntervalItem(

    @field:SerializedName("Pos")
    val pos: Pos = Pos(),
    @field:SerializedName("Price")
    val price: Price = Price()
)