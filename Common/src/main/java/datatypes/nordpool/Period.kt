package datatypes.nordpool

import com.google.gson.annotations.SerializedName
import javax.annotation.processing.Generated

@Generated("com.robohorse.robopojogenerator")
data class Period(

    @field:SerializedName("TimeInterval")
    val timeInterval: TimeInterval=TimeInterval(),

    @field:SerializedName("Resolution")
    val resolution: Resolution = Resolution(),

    @field:SerializedName("Interval")
    val interval: List<IntervalItem> = emptyList()
)