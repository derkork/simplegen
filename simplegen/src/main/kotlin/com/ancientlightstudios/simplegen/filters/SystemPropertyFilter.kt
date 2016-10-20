package com.ancientlightstudios.simplegen.filters

import com.hubspot.jinjava.interpret.JinjavaInterpreter
import com.hubspot.jinjava.lib.filter.Filter

/**
 * Filter which allows to access system properties
 */
class SystemPropertyFilter : Filter {
    override fun getName(): String = "sp"

    override fun filter(obj: Any?, interpreter: JinjavaInterpreter?, vararg args: String?): Any? {

        return System.getProperty(obj.toString())
    }
}