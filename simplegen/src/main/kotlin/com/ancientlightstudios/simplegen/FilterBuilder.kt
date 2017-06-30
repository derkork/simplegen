package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.configuration.CustomFilterConfiguration
import com.ancientlightstudios.simplegen.filters.ScriptFilter
import com.ancientlightstudios.simplegen.resources.FileResolver
import com.hubspot.jinjava.lib.filter.Filter

object FilterBuilder {

    fun buildFilter(customFilterConfiguration: CustomFilterConfiguration, fileResolver: FileResolver) : Filter {
        val script = fileResolver.resolve(customFilterConfiguration.script).readText()
        val function = customFilterConfiguration.function

        if (script.isBlank()) {
            throw IllegalArgumentException("The filter script is blank.")
        }

        if (function.isBlank()) {
           throw IllegalArgumentException("The filter's function name cannot be empty.")
        }

        return ScriptFilter(customFilterConfiguration.script, script, function)
    }
}