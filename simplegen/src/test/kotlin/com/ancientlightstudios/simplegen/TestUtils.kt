package com.ancientlightstudios.simplegen

import java.io.File

fun getTestConfig(): File = File(JsonUtilSpecs::class.java.getResource("/test_config.yml").toURI())

fun getResourcesRoot(): String = getTestConfig().parent
fun getResourcesRootFile (): File = getTestConfig().parentFile