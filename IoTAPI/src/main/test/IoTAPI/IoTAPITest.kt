package IoTAPI

import Tangle.TangleController
import datatypes.tangle.Tag
import helpers.StatisticsCollector
import org.junit.Test
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class IoTAPITest {
    @Test
    fun testOneMillionBroadcastsOnTangle() {
        val t = TangleController("TEAEA99999999E9999999999999999999999999999999999999999999999999999999999999999999")
        val tasks = LinkedBlockingQueue<Runnable>()
        val tp = ThreadPoolExecutor(10, 10, 20, TimeUnit.SECONDS, tasks)
        val a = AtomicInteger(0)
        addTasks(100, tp, t, a, 10000)
        while (tp.taskCount > 0) {
            Thread.sleep(1000L)
        }
    }

    private fun addTasks(count: Int, threadPoolExecutor: ThreadPoolExecutor, t: TangleController, a: AtomicInteger, max: Int) {
        for (i in 1..count) {
            threadPoolExecutor.submit {
                t.attachBroadcastToTangle("{\"publicKey\":\"165615760792845808078562255657443417455825412085455006148620929027491723303411487844979141873745096637953045819064779313008630684872882194634243766937424079829\",\"deviceSpecification\":{\"id\":\"${UUID.randomUUID()}\",\"deviceResources\":[{\"resourceMethods\":[{\"methodType\":\"GET\",\"parameters\":{\"status\":\"Boolean\"},\"description\":\"Gets the current status\"},{\"methodType\":\"POST\",\"parameters\":{\"status\":\"Boolean\"},\"description\":\"Turn device on/off\"}],\"path\":\"onOff\",\"title\":\"onOff\"},{\"resourceMethods\":[{\"methodType\":\"GET\",\"parameters\":{\"temperature\":\"Integer\"},\"description\":\"Gets the target temperature of the heat pump\"},{\"methodType\":\"POST\",\"parameters\":{\"temperature\":\"Integer\"},\"description\":\"Adjusts target temperature of the heat pump by diff\"}],\"path\":\"temperature\",\"title\":\"Heat pump resource\"}]}}__MC4CFQPlZJqniy9Kr6K2UQpATLMSYKBw4wIVA6oAyl26t0xXZsXMvfrf1zOxqb8p", Tag.DSPEC)
                val tasks = a.incrementAndGet()
                if (tasks % count == 0) {
                    t.getMessagesUnchecked(Tag.DSPEC)
                    t.getBroadcastsUnchecked(Tag.DSPEC)
                    StatisticsCollector.printStats(tasks)
                    StatisticsCollector.clear()
                    if (tasks < max) {
                        addTasks(count, threadPoolExecutor, t, a, max)
                    }
                }
            }
        }
    }
}