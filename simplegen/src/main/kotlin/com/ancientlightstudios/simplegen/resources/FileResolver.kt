package com.ancientlightstudios.simplegen.resources

import java.io.File

/**
 * A file resolver resolves files from a virtual file system.
 */
interface FileResolver {
    /**
     * Resolves the given relative path, relative to the root of this file resolver.
     * @throws FileNotResolvedException when the file cannot be resolved.
     */
    fun resolve(relativePath: String): File

    /**
     * Resolves a list of files according to the given includes and excludes ant patterns.
     */
    fun resolve(includes: List<String>, excludes: List<String>): List<File>
}