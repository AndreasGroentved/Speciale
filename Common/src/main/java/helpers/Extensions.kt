package helpers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.simple.SimpleLoggerFactory

object Extensions {


}


class A {
    init {
        val q = LoggerFactory.getLogger("")
        q.name
    }
}

fun Logger.i(arg: Any) {
    this.info(arg.toString())
}


fun LogI(arg: Any?) = arg?.let { SimpleLoggerFactory().getLogger(Thread.currentThread().stackTrace[2].className).i(it) }
fun LogE(arg: Any?) = arg?.let { SimpleLoggerFactory().getLogger(Thread.currentThread().stackTrace[2].className).e(it) }
fun Logger.e(arg: Any) = this.error(arg.toString())
fun Log() = SimpleLoggerFactory().getLogger(Thread.currentThread().stackTrace[2].className)
