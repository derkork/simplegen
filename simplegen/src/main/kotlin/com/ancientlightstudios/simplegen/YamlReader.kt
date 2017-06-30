package com.ancientlightstudios.simplegen

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonMappingException
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
    fun readToMap(source: String, stream: InputStream): Map<String, Any> {
        val typeRef = object : TypeReference<HashMap<String, Any>>() {}
        try {
            return ObjectMapper(YAMLFactory()).readValue(stream, typeRef)
        }
        catch(e:JsonMappingException) {
            handle(source, e)
            throw RuntimeException() // should never happen
        }
    }


    /**
     * Reads a YAML stream into a pojo.
     * @param stream the stream to read
     * @param type the pojo type
     * @return the resulting pojo
     */
    fun <T> readToPojo(source: String, stream: InputStream, type: Class<T>): T {
        try {
            return ObjectMapper(YAMLFactory()).readValue(stream, type)
        }
        catch(e:JsonMappingException) {
            handle(source, e)
            throw RuntimeException() // should never happen
        }
    }

    private fun handle(source:String,  e: JsonMappingException) {
        throw YamlErrorException(source, e.message)
    }

}