package helpers

import java.io.FileReader
import java.io.FileWriter
import java.util.*

class PropertiesLoader private constructor() {

    private val properties = loadProperties()

    companion object {
        val instance by lazy { PropertiesLoader() }
    }

    private fun loadProperties() = Properties().apply { this.load(FileReader("config.properties")) }

    fun getProperty(key: String): String {
        return properties.getProperty(key)
    }

    fun removeProperty(key: String) {
        properties.remove(key)
        properties.store(FileWriter("config.properties"), null)
    }

    fun hasProperty(key: String): Boolean {
        return properties.containsKey(key)
    }

    fun getOptionalProperty(key: String): String? {
        return properties.getProperty(key)
    }

    fun writeProperty(key: String, value: String) {
        properties.setProperty(key, value)
        properties.store(FileWriter("config.properties"), null)
    }

}