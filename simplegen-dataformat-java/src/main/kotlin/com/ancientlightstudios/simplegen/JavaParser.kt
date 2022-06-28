package com.ancientlightstudios.simplegen

import org.jboss.forge.roaster.ParserException
import org.jboss.forge.roaster.Roaster
import org.jboss.forge.roaster.model.JavaUnit
import java.io.InputStream

class JavaParser : DataParser {

    override val supportedDataFormats: Set<String>
        get() = setOf("text/java")

    override fun parse(stream: InputStream, origin: String): Map<String, Any> {
        val result: JavaUnit
        try {
            result = Roaster.parseUnit(stream)
        }
        catch(ex:ParserException) {
            throw DataParseException(origin, ex.message)
        }

        return mapOf("javaTypes" to result.topLevelTypes)
    }
}