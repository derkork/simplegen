package com.ancientlightstudios.simplegen

import java.io.InputStream

/**
 * This interface is responsible for parsing data into a map structure. We use this for
 * loading data from Yaml or other file formats into a unified map which is the input for our
 * templating engine.
 */
interface DataParser {
    /**
     * The set of data formats supported by this parser. Each string must contain
     * a mime type.
     */
    val supportedDataFormats: Set<String>

    /**
     * Initialize the parser with the given configuration. The configuration is parser specific.
     */
    fun init(configuration: Map<String,Any>)

    /**
     * Parses the given input stream and returns a normalized map representation of the
     * parsed data.
     *
     * @param stream the input stream which should be read. The implementation can close the stream after parsing but
     * is not required to do so.
     * @param origin a textual representation of the location where the data originated. Usually a file name. This is
     * only used for providing context information in case of an error. The implementation should copy this value into
     * the origin parameter of any thrown [DataParseException].
     * @exception DataParseException in case the data cannot be parsed.
     */
    fun parse(stream: InputStream, origin: String, configuration: Map<String, Any>): Map<String, Any>
}