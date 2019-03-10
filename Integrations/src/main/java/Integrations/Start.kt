package Integrations

import Integrations.nordpool.NordPoolAPIMock
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

fun main() {
    val scheduledThreadPoolExecutor = ScheduledThreadPoolExecutor(1)
    val nordPoolAPIMock = NordPoolAPIMock()
    scheduledThreadPoolExecutor.scheduleAtFixedRate({ nordPoolAPIMock.publishMockPrices() }, 0, 1, TimeUnit.HOURS)
}