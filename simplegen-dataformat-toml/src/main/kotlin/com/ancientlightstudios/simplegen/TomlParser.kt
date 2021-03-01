package com.ancientlightstudios.simplegen

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.tomlj.Toml
import java.io.InputStream
import java.util.*

class TomlParser : DataParser {
    override val supportedDataFormats: Set<String> = setOf("application/toml")

    override fun parse(stream: InputStream, origin: String): Map<String, Any> {
        val typeRef = object : TypeReference<HashMap<String, Any>>() {}
        try {
            // simplest possible way, parse, TOML, convert to JSON, feed back into Jackson.

            val tomlParseResult = Toml.parse(stream)
            if (tomlParseResult.hasErrors()) {
                val error = tomlParseResult
                    .errors()
                    .joinToString("\n") { "${it.position()} ${it.message}" }

                throw DataParseException(origin, error)
            }
            val builder = StringBuilder()
            JsonSerializer.toJson(tomlParseResult, builder)

            return ObjectMapper(JsonFactory())
                .readValue(builder.toString(), typeRef)
        } catch (e: Exception) {
            throw DataParseException(origin, e.message)
        }
    }
}
