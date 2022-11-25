package com.ancientlightstudios.simplegen

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderHeaderAwareBuilder
import org.apache.commons.io.input.BOMInputStream
import java.io.InputStream
import java.nio.charset.Charset

class CsvParser : DataParser {
    override val supportedDataFormats: Set<String> = setOf("text/csv", "application/csv")

    override val defaultResultPath: String
        get() = "csv"

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

            // if stripBom is set to true, we need to strip the BOM from the stream
            val strippedStream = if (booleanOrDefault(mergedSettings["stripBom"], true)) {
                BOMInputStream(stream)
            } else {
                stream
            }

            val reader = CSVReaderHeaderAwareBuilder(strippedStream.reader(charset))
                .withCSVParser(
                    CSVParserBuilder()
                        .withSeparator(firstCharOfStringOrDefault(mergedSettings["separatorChar"], ','))
                        .withEscapeChar(firstCharOfStringOrDefault(mergedSettings["escapeChar"], '\\'))
                        .withQuoteChar(firstCharOfStringOrDefault(mergedSettings["quoteChar"], '"'))
                        .build()
                )
                .withSkipLines(asIntOrDefault(mergedSettings["skipLines"], 0))
                .build()

            // walk the path parts and create the nested maps
            val result = mutableMapOf<String,Any>()

            // now build the list of maps
            val resultList = mutableListOf<Map<String, Any>>()
            while (true) {
                val row = reader.readMap() ?: break
                resultList.add(row)
            }

            // and add it to the last map
            result["entries"] = resultList

            return result
        } catch (e: Exception) {
            throw DataParseException(origin, e.message)
        }
    }

}
