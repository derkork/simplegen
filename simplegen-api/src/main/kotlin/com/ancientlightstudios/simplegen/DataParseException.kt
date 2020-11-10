package com.ancientlightstudios.simplegen

/**
 * Exception that is thrown when parsing data fails for whatever reason.
 * @param origin the location of the source that was parsed.
 * @param message the error message describing what happened.
 */
class DataParseException(val origin: String, message: String?) : RuntimeException(message)