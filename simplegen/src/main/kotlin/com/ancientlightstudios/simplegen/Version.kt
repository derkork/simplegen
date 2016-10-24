package com.ancientlightstudios.simplegen

import java.util.*


object Version {
    val version: String

    const val UnknownVersion: String = "<unreleased development build>"

    init {
        version = try {
            val props = Properties()
            Version::class.java.getResourceAsStream("/META-INF/maven/com.ancientlightstudios/simplegen/pom.properties").use {
                props.load(it)
            }
            props.getProperty("version", UnknownVersion)
        } catch(_: Exception) {
            UnknownVersion
        }
    }

}