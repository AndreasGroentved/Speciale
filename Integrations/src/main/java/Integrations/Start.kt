package Integrations

import Integrations.energinet.EnerginetAPIAdapter
import Integrations.nordpool.NordPoolAPIMock
import org.slf4j.simple.SimpleLoggerFactory
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

fun main() {
    val logger = SimpleLoggerFactory().getLogger("Integrations")
    logger.info("Starting NordPool and Energinet services")
    val scheduledThreadPoolExecutor = ScheduledThreadPoolExecutor(1)
    val nordPoolAPIMock = NordPoolAPIMock()
    val energinetAPI = EnerginetAPIAdapter()
    scheduledThreadPoolExecutor.scheduleAtFixedRate({ energinetAPI.publishCO2Signal() }, 0, 5, TimeUnit.MINUTES)
    scheduledThreadPoolExecutor.scheduleAtFixedRate({ nordPoolAPIMock.publishMockPrices() }, 0, 1, TimeUnit.HOURS)
}