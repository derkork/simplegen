package com.ancientlightstudios.simplegen.resources

import java.io.File

class SimpleFileResolver(val basePath: String = ".") : FileResolver {
    override fun resolve(relativePath: String): File {
        val result = FileUtil.resolve(basePath, relativePath)
        if (!result.exists()) {
            throw FileNotResolvedException(relativePath)
        }
        return result
    }

    override fun resolve(includes: List<String>, excludes: List<String>): List<File> = FileUtil.resolve(basePath, includes, excludes)
}