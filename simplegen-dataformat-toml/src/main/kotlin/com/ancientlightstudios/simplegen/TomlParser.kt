package com.ancientlightstudios.simplegen

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.teesoft.jackson.dataformat.toml.TOMLFactory
import java.io.InputStream
import java.util.*

class TomlParser : DataParser {
    override val supportedDataFormats: Set<String> = setOf("application/toml")

    override fun parse(stream: InputStream, origin: String): Map<String, Any> {
        val typeRef = object : TypeReference<HashMap<String, Any>>() {}
        try {
            return ObjectMapper(TOMLFactory())
                .readValue(stream, typeRef)
        } catch (e: Exception) {
            throw DataParseException(origin, e.message)
        }
    }
}