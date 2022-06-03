package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.resources.FileResolver
import com.ancientlightstudios.simplegen.resources.SimpleFileResolver
import java.io.File

fun getTestConfig(): File = File(JsonUtilTests::class.java.getResource("/test_config.yml").toURI())
fun getTestConfigRelative(): File = File(JsonUtilTests::class.java.getResource("/test_config_relative.yml").toURI())

fun getResourcesRoot(): String = getTestConfig().parent
fun getResourcesRootFile(): File = getTestConfig().parentFile

fun getResourcesRootFileResolver(): FileResolver = SimpleFileResolver(getResourcesRoot())

