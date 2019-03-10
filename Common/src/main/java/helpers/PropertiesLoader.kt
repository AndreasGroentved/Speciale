package helpers

import java.util.*

class PropertiesLoader {

    private val properties = loadProperties()

    companion object {
        val instance = PropertiesLoader()
    }

    fun loadProperties() = Properties().apply { this.load(ClassLoader.getSystemResourceAsStream("config.properties")) }

    fun getProperty(key: String): String {
        properties.load(ClassLoader.getSystemResourceAsStream("config.properties"))
        return properties.getProperty(key)
    }

}