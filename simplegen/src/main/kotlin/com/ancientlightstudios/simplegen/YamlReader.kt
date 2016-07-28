package com.ancientlightstudios.simplegen

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.InputStream
import java.util.*

object YamlReader {

    /**
     * Reads a yaml stream into a map.
     * @param stream the stream to read
     * @return the resulting map.
     */
    fun readToMap(stream: InputStream): Map<String, Any> {
        val typeRef = object : TypeReference<HashMap<String, Any>>() {}
        return ObjectMapper(YAMLFactory()).readValue(stream, typeRef)
    }

    /**
     * Reads a yaml stream into a pojo.
     * @param stream the stream to read
     * @param type the pojo type
     * @return the resulting pojo
     */
    fun <T> readToPojo(stream: InputStream, type: Class<T>): T = ObjectMapper(YAMLFactory()).readValue(stream, type)
}