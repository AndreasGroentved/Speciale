package datatypes.nordpool

import com.google.gson.annotations.SerializedName
import javax.annotation.processing.Generated

@Generated("com.robohorse.robopojogenerator")
data class Period(

    @field:SerializedName("TimeInterval")
    val timeInterval: TimeInterval? = null,

    @field:SerializedName("Resolution")
    val resolution: Resolution? = null,

    @field:SerializedName("Interval")
    val interval: List<IntervalItem?>? = null
)