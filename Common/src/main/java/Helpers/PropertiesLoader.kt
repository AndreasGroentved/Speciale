package Helpers

import java.util.*

class PropertiesLoader {
    fun loadProperties(): Properties {
        val properties = Properties()
        properties.load(ClassLoader.getSystemResourceAsStream("config.properties"))
        return properties
    }
}