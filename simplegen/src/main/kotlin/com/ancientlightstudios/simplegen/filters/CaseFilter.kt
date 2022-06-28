package com.ancientlightstudios.simplegen.filters

import com.google.common.base.CaseFormat
import com.hubspot.jinjava.interpret.JinjavaInterpreter
import com.hubspot.jinjava.lib.filter.Filter
import java.util.*

/**
 * Filter which converts several case encodings between each other.
 */
class CaseFilter : Filter {
    override fun getName(): String = "case"

    override fun filter(obj: Any?, interpreter: JinjavaInterpreter?, vararg args: String?): Any? {
        if (args.size != 2) {
            throw IllegalArgumentException("Please specify source and destination casing.")
        }

        if (obj == null) {
            return null
        }

        val srcCase = args[0]?.replace("-", "_")?.uppercase(Locale.getDefault()) ?: throw IllegalArgumentException("Source case format must not be null.")
        val destCase = args[1]?.replace("-", "_")?.uppercase(Locale.getDefault()) ?: throw IllegalArgumentException("Destination case format must not be null.")


        val srcCaseFormat = CaseFormat.valueOf(srcCase)
        val destCaseFormat = CaseFormat.valueOf(destCase)

        return srcCaseFormat.to(destCaseFormat, obj.toString())
    }
}