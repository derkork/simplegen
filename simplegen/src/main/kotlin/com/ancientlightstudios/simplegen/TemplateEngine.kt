package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.filters.CaseFilter
import com.ancientlightstudios.simplegen.filters.JsonPathFilter
import com.ancientlightstudios.simplegen.filters.SystemPropertyFilter
import com.ancientlightstudios.simplegen.resources.FileResolver
import com.hubspot.jinjava.Jinjava
import com.hubspot.jinjava.JinjavaConfig

class TemplateEngine(fileResolver: FileResolver, arguments: TemplateEngineArguments) {

    private val templateEngine : Jinjava

    init {
        val configBuilder = JinjavaConfig.newBuilder()
        configBuilder.withLstripBlocks(arguments.configuration.lstripBlocks)
        configBuilder.withTrimBlocks(arguments.configuration.trimBlocks)
        configBuilder.withEnableRecursiveMacroCalls(arguments.configuration.enableRecursiveMacroCalls)

        templateEngine = Jinjava(configBuilder.build())
        templateEngine.resourceLocator = RepositoryFileLocator(fileResolver)
        templateEngine.globalContext.registerFilter(JsonPathFilter())
        templateEngine.globalContext.registerFilter(SystemPropertyFilter())
        templateEngine.globalContext.registerFilter(CaseFilter())

        arguments.additionalFilters.forEach { additionalFilter -> templateEngine.globalContext.registerFilter(additionalFilter) }
    }


    fun execute(input: String, node: Any, data: Any): String {
        return execute(input, mapOf("data" to data, "node" to node))
    }

    fun execute(input: String, context: Map<String, Any>): String {
        return templateEngine.render(input, context)
    }
}