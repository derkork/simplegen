package com.ancientlightstudios.simplegen

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.InputStream

/**
 * [DataParser] for the YAML data format. This is the only built-in data format.
 */
class YamlParser : DataParser {
    override val supportedDataFormats: Set<String> = setOf(
        "application/yaml",
        "text/yaml",
        "text/x-yaml",
        "application/x-yaml",
        "text/vnd.yaml"
    )

    override fun init(configuration: Map<String, Any>) {
        // nothing to do
    }

    override fun parse(stream: InputStream, origin: String, configuration: Map<String, Any>): Map<String, Any> {
        val typeRef = object : TypeReference<HashMap<String, Any>>() {}
        try {
            return ObjectMapper(YAMLFactory()).readValue(stream, typeRef)
        }
        catch(e: Exception) {
          throw DataParseException(origin, e.message)
        }
    }
}