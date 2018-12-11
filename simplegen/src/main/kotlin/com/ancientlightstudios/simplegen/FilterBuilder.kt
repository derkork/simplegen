package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.configuration.CustomFilterConfiguration
import com.ancientlightstudios.simplegen.filters.ScriptFilter
import com.ancientlightstudios.simplegen.resources.FileResolver
import com.hubspot.jinjava.lib.filter.Filter

object FilterBuilder {

    fun buildFilter(customFilterConfiguration: CustomFilterConfiguration, fileResolver: FileResolver) : DependencyObject<Filter> {
        val file = fileResolver.resolve(customFilterConfiguration.script)
        val lastModified = file.lastModified()
        val script = file.readText()
        val function = customFilterConfiguration.function

        if (script.isBlank()) {
            throw IllegalArgumentException("The filter script is blank.")
        }

        if (function.isBlank()) {
           throw IllegalArgumentException("The filter's function name cannot be empty.")
        }

        return DependencyObject(ScriptFilter(customFilterConfiguration.script, script, function), lastModified)
    }
}