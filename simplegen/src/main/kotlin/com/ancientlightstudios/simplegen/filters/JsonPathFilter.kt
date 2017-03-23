package com.ancientlightstudios.simplegen.filters

import com.hubspot.jinjava.interpret.JinjavaInterpreter
import com.hubspot.jinjava.lib.filter.Filter
import com.jayway.jsonpath.JsonPath

/**
 * Filter which allows to
 */
class JsonPathFilter : Filter {
    override fun getName(): String = "jsonpath"

    override fun filter(obj: Any?, interpreter: JinjavaInterpreter?, vararg args: String?): Any? {
        if (args.isEmpty()) {
            return obj
        }

        return JsonPath.read(obj, args[0])
    }
}