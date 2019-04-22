package helpers

import java.util.concurrent.ConcurrentHashMap

object StatisticsCollector {

    private val map: ConcurrentHashMap<String, MutableList<Long>> = ConcurrentHashMap()

    fun clear() = map.clear()

    fun submitDuration(name: String, duration: Long) {
        if (map[name] != null) map[name]!!.add(duration) else map[name] = mutableListOf(duration)
    }

    fun printStats(transactions: Int?) {
        val sb = StringBuilder()
        //sb.append("---------STATS-----------\n\n\n")
        transactions?.let {
            sb.append("----AT $it TRANSACTIONS-----\n")
        }
        map.forEach {
            var dur: Long = 0
            it.value.forEach { duration -> dur += duration }
            dur /= it.value.size
            sb.append("Name: ${it.key} Duration: $dur\n")
        }
        println(sb.toString())
    }

}