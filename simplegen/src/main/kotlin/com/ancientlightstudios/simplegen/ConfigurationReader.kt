package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.configuration.Configuration
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.InputStream

object ConfigurationReader {
    /**
     * Reads a YAML stream into a configuration object.
     * @param stream the stream to read
     * @param origin the origin where the configuration was loaded from.
     * @return the resulting configuration object.
     */
    fun readConfiguration(stream: InputStream, origin: String): Configuration {
        try {
            return ObjectMapper(YAMLFactory()).readValue(stream, Configuration::class.java)
        } catch (e: Exception) {
            throw ConfigurationException(origin, e.message)
        }
    }
}