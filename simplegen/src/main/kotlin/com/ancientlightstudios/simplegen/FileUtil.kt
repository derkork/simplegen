package com.ancientlightstudios.simplegen

import java.io.File

object FileUtil {

    fun resolve(basePath: String, relativePath: String): File {
        val f = File(relativePath)
        if (f.isAbsolute) {
            return f
        }
        return File(basePath, relativePath)
    }
}