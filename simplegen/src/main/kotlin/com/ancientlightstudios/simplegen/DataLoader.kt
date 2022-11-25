package com.ancientlightstudios.simplegen

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

/**
 * Responsible for loading data from files. For now only local files are supported, ultimately we would like to support
 * arbitrary file locations (e.g. load data from URLs).
 */
object DataLoader {
    private val log: Logger = LoggerFactory.getLogger(DataLoader::class.java)

    private val parsersByMimeType: Map<String, DataParser>

    init {
        val tempMap = mutableMapOf<String, DataParser>()
        val serviceLoader = ServiceLoader.load(DataParser::class.java)

        // load all data parsers we know and build a map of mime type to parser.
        serviceLoader.forEach { parser ->
            val supportedFormats = parser.supportedDataFormats
            supportedFormats.forEach { format ->
                if (tempMap.containsKey(format)) {
                    log.warn(
                        "There are multiple parsers for mime type {} available. Using {}",
                        format,
                        tempMap[format]!!.javaClass.name
                    )
                } else {
                    tempMap[format] = parser
                }
            }
        }

        parsersByMimeType = tempMap
    }

    fun parse(file: File, mimeType: String? = null, resultPath: String, parserSettings: Map<String, Any>): Map<String, Any> {
        val path = file.path
        val realMimeType = mimeType
            ?: "application/yaml"

        return file.inputStream().use {
            val parser = parsersByMimeType[realMimeType]
                ?: throw DataParseException(path, "No parser is available for mime type $realMimeType")
            val parseResult = parser.parse(it, path, parserSettings)
            if (resultPath.isEmpty()) {
                return@use parseResult
            }
            // the result will be a list of maps, each map representing a row
            // the user can configure the path of the result in the output map

            // we split the path into its parts and create nested maps for each part
            val resultPathParts = resultPath.split(".")
            val result = mutableMapOf<String, Any>()

            // walk the path parts and create the nested maps
            var currentMap = result
            for (i in 0 until resultPathParts.size - 1) { // we don't need the last part, as it will be the key of the last map
                val part = resultPathParts[i]
                val newMap = mutableMapOf<String, Any>()
                currentMap[part] = newMap
                currentMap = newMap
            }

            // and add it to the last map
            currentMap[resultPathParts.last()] = parseResult
            result
        }
    }

    /**
     * Initializes all parsers with the global configuration.
     */
    fun initParsers(extensionSettings: Map<String, Any>) {
        parsersByMimeType.forEach { (_, parser) ->
            parser.init(extensionSettings)
        }
    }
}