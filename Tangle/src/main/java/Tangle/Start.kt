package Tangle

import Helpers.PropertiesLoader

fun main() {

    val props = PropertiesLoader().loadProperties()
    println(props.getProperty("hest"))
}

