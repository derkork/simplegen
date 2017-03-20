package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.filters.CaseFilter
import com.ancientlightstudios.simplegen.filters.JsonPathFilter
import com.ancientlightstudios.simplegen.filters.SystemPropertyFilter
import com.hubspot.jinjava.Jinjava
import com.hubspot.jinjava.JinjavaConfig
import org.slf4j.LoggerFactory

object TemplateEngine {
    private val log = LoggerFactory.getLogger(Runner::class.java)

    fun execute(input: String, node: Any, data: Any, basePath: String,
                configuration: TemplateEngineConfiguration = TemplateEngineConfiguration()): String {
        return execute(input, mapOf("data" to data, "node" to node), listOf(basePath), configuration)
    }

    fun execute(input: String, context: Map<String, Any>, templateBasePaths: List<String> = listOf("."),
                configuration: TemplateEngineConfiguration = TemplateEngineConfiguration()): String {
        if (log.isDebugEnabled) {
            log.debug("Template: $input")
            log.debug("Context: $context")
        }

        val configBuilder = JinjavaConfig.newBuilder()
        configBuilder.withLstripBlocks(configuration.lstripBlocks)
        configBuilder.withTrimBlocks(configuration.trimBlocks)
        configBuilder.withEnableRecursiveMacroCalls(configuration.enableRecursiveMacroCalls)

        val templateEngine = Jinjava(configBuilder.build())
        templateEngine.resourceLocator = RepositoryFileLocator(templateBasePaths)
        templateEngine.globalContext.registerFilter(JsonPathFilter())
        templateEngine.globalContext.registerFilter(SystemPropertyFilter())
        templateEngine.globalContext.registerFilter(CaseFilter())
        return templateEngine.render(input, context)
    }
}