package com.ancientlightstudios.simplegen

import org.apache.tools.ant.DirectoryScanner
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

object FileUtil {

    val log: Logger = LoggerFactory.getLogger(FileUtil::class.java)

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

        if (log.isDebugEnabled ) {
            log.debug("Scanning basePath: {} includes: {} excludes:{}", basePath, includes.joinToString(","), excludes.joinToString(","))
        }

        val files = scanner.includedFiles.map { File(basePath, it) }

        if (log.isDebugEnabled) {
            log.debug("Resolved files: ${files.map { it.path }.joinToString(",") }")
        }

        return files
    }
}