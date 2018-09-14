package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.resources.FileResolver
import com.hubspot.jinjava.interpret.JinjavaInterpreter
import com.hubspot.jinjava.loader.ResourceLocator
import com.hubspot.jinjava.loader.ResourceNotFoundException
import java.io.File
import java.nio.charset.Charset

class RepositoryFileLocator(private val fileResolver: FileResolver) : ResourceLocator {
    override fun getString(fullName: String, encoding: Charset, interpreter: JinjavaInterpreter?): String {
        val resolve: File
        try {
            resolve = fileResolver.resolve(fullName)
        } catch(e: Exception) {
            throw ResourceNotFoundException("Couldn't find resource: $fullName")
        }
        return resolve.readText(encoding)
    }

}