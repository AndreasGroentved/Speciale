package helpers

import org.slf4j.Logger

object Extensions {


}

fun Logger.i(arg: Any) = this.info(arg.toString())
fun Logger.e(arg: Any) = this.error(arg.toString())