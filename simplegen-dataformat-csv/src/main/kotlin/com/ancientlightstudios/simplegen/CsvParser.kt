package com.ancientlightstudios.simplegen

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderHeaderAwareBuilder
import java.io.InputStream
import java.nio.charset.Charset

class CsvParser : DataParser {
    override val supportedDataFormats: Set<String> = setOf("application/csv")

    lateinit var settings: Map<String, Any>

    override fun init(configuration: Map<String, Any>) {
        // csv settings are map below the "csv" key in the configuration
        settings = configuration["csv"] as? Map<String, Any> ?: mapOf()
    }

    override fun parse(stream: InputStream, origin: String, configuration: Map<String, Any>): Map<String, Any> {
        try {

            // create a merged map of settings from the parser and the configuration
            val mergedSettings = settings + configuration

            // determine file character set
            // make a Charset object from the charset name
            val charset = Charset.forName(mergedSettings["charset"] as? String ?: Charsets.UTF_8.name())


            val reader = CSVReaderHeaderAwareBuilder(stream.reader(charset))
                .withCSVParser(
                    CSVParserBuilder()
                        .withSeparator(firstCharOfStringOrDefault(mergedSettings["separatorChar"], ','))
                        .withEscapeChar(firstCharOfStringOrDefault(mergedSettings["escapeChar"], '\\'))
                        .withQuoteChar(firstCharOfStringOrDefault(mergedSettings["quoteChar"], '"'))
                        .build()
                )
                .withSkipLines(asIntOrDefault(mergedSettings["skipLines"], 0))
                .build()

            // the result will be a list of maps, each map representing a row
            // the user can configure the path of the result in the output map
            val resultPath = mergedSettings["resultPath"] as? String ?: "csv"

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

            // now build the list of maps
            val resultList = mutableListOf<Map<String, Any>>()
            while (true) {
                val row = reader.readMap() ?: break
                resultList.add(row)
            }

            // and add it to the last map
            currentMap[resultPathParts.last()] = resultList

            return result
        } catch (e: Exception) {
            throw DataParseException(origin, e.message)
        }
    }

    private fun firstCharOfStringOrDefault(value: Any?, defaultValue: Char): Char {
        if (value is String) {
            return value.firstOrNull() ?: defaultValue
        }
        if (value is Char) {
            return value
        }
        return defaultValue
    }

    private fun asIntOrDefault(value: Any?, defaultValue: Int): Int {
        if (value is Int) {
            return value
        }
        if (value is String) {
            return value.toIntOrNull() ?: defaultValue
        }
        return defaultValue
    }
}
