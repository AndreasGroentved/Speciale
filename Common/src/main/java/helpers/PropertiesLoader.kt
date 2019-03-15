package helpers

import java.io.FileReader
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

}