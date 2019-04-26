package helpers

import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory

fun Logger.i(arg: Any) {
    this.info(arg.toString())
}

fun Logger.w(arg: Any) {
    this.warn(arg.toString())
}

fun LogI(arg: Any?) = arg?.let { SimpleLoggerFactory().getLogger(Thread.currentThread().stackTrace[2].className).i(it) }
fun LogW(arg: Any?) = arg?.let { SimpleLoggerFactory().getLogger(Thread.currentThread().stackTrace[2].className).w(it) }
fun LogE(arg: Any?) = arg?.let { SimpleLoggerFactory().getLogger(Thread.currentThread().stackTrace[2].className).e(it) }
fun LogE(exception: Throwable, message: String = "error occurred") = exception.let {
    SimpleLoggerFactory().getLogger(Thread.currentThread().stackTrace[2].className).e(it, errorMessage = message)
    /* it.printStackTrace()*/
}

fun Logger.e(arg: Any, errorMessage: String = "error occurred") = if (arg is Throwable) this.error(errorMessage, arg) else this.error(arg.toString())