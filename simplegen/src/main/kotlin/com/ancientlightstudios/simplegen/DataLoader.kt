package com.ancientlightstudios.simplegen

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
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

    fun parse(file: File, parserSettings: Map<String, Any>, mimeType: String? = null): Map<String, Any> {
        val path = file.path
        val realMimeType = mimeType
            ?: "application/yaml"

        return file.inputStream().use {
            parse(it, path, realMimeType, parserSettings)
        }
    }

    fun parse(inputStream: InputStream, origin: String, mimeType: String, parserSettings: Map<String, Any>): Map<String, Any> {
        val parser = parsersByMimeType[mimeType]
            ?: throw DataParseException(origin, "No parser is available for mime type $mimeType")

        return parser.parse(inputStream, origin, parserSettings)
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