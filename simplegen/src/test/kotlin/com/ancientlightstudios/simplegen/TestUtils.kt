package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.resources.FileResolver
import com.ancientlightstudios.simplegen.resources.SimpleFileResolver
import java.io.File
import kotlin.test.assertTrue

fun getTestConfig(): File = File(JsonUtilSpecs::class.java.getResource("/test_config.yml").toURI())

fun getResourcesRoot(): String = getTestConfig().parent
fun getResourcesRootFile(): File = getTestConfig().parentFile

fun getResourcesRootFileResolver(): FileResolver = SimpleFileResolver(getResourcesRoot())

fun assertContains(input: String?, value: String, message: String? = null) = assertTrue(input != null && input.contains(value), message)
fun catchException(toRun: () -> Unit): Exception? {
    var result: Exception? = null
    try {
        toRun()
    } catch(e: Exception) {
        result = e
    }
    return result
}
