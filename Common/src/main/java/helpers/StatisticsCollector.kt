package helpers

class StatisticsCollector {
    companion object {
        private val map: MutableMap<String, MutableList<Long>> = mutableMapOf()

        fun submitDuration(name: String, duration: Long) {
            if (map[name] != null) map[name]!!.add(duration) else map[name] = mutableListOf(duration)
        }

        fun printStats(transactions: Int?) {
            val sb = StringBuilder()
            sb.append("---------STATS-----------\n\n\n")
            transactions?.let {
                sb.append("----AT $it TRANSACTIONS-----\n\n")
            }
            map.forEach {
                var dur: Long = 0
                it.value.forEach { duration -> dur += duration }
                dur /= it.value.size
                sb.append("Name: ${it.key} Duration: $dur\n")
            }
            LogI(sb.toString())
        }
    }
}