package com.ancientlightstudios.simplegen

import org.apache.tools.ant.DirectoryScanner
import java.io.File

object FileUtil {

    fun resolve(basePath: String, relativePath: String): File {
        val f = File(relativePath)
        if (f.isAbsolute) {
            return f
        }
        return File(basePath, relativePath)
    }

    fun resolve(basePath : String, includes: List<String>, excludes: List<String>) : List<File> {
        val scanner = DirectoryScanner()
        scanner.setBasedir(basePath)
        scanner.setIncludes(includes.toTypedArray())
        scanner.setExcludes(excludes.toTypedArray())

        scanner.scan()

        return scanner.includedFiles.map { File(basePath, it) }


    }
}