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
fun Logger.e(arg: Any) = this.error(arg.toString())