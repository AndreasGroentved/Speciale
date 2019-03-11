package helpers

import java.util.*

class PropertiesLoader private constructor() {

    private val properties = loadProperties()

    companion object {
        val instance by lazy { PropertiesLoader() }
    }

    fun loadProperties() = Properties().apply { this.load(ClassLoader.getSystemResourceAsStream("config.properties")) }

    fun getProperty(key: String): String {
        properties.load(ClassLoader.getSystemResourceAsStream("config.properties"))
        return properties.getProperty(key)
    }

}