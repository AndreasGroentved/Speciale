package Helpers

import java.util.*

object PropertiesLoader {
    fun loadProperties() = Properties().apply { this.load(ClassLoader.getSystemResourceAsStream("config.properties")) }
}