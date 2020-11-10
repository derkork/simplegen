package com.ancientlightstudios.simplegen.filters

import com.hubspot.jinjava.interpret.JinjavaInterpreter
import com.hubspot.jinjava.lib.filter.Filter

/**
 * Filter which allows to access system environment variables.
 */
class EnvironmentVariableFilter : Filter {
    override fun getName(): String = "env"

    override fun filter(obj: Any?, interpreter: JinjavaInterpreter?, vararg args: String?): Any? {
        return System.getenv(obj.toString())
    }
}