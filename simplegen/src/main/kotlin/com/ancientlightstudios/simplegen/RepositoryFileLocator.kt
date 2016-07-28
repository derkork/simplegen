package com.ancientlightstudios.simplegen

import com.hubspot.jinjava.interpret.JinjavaInterpreter
import com.hubspot.jinjava.loader.FileLocator
import com.hubspot.jinjava.loader.ResourceLocator
import com.hubspot.jinjava.loader.ResourceNotFoundException
import java.io.File
import java.nio.charset.Charset

class RepositoryFileLocator(basePaths: List<String> = listOf(".")) : ResourceLocator {

    private val fileLocators: List<FileLocator> = basePaths.map { FileLocator(File(it)) }


    override fun getString(fullName: String?, encoding: Charset?, interpreter: JinjavaInterpreter?): String {
        for (fileLocator in fileLocators) {
            try {
                return fileLocator.getString(fullName, encoding, interpreter)
            } catch(ignore: ResourceNotFoundException) {
            }
        }
        throw ResourceNotFoundException("Couldn't find resource: " + fullName)
    }

}