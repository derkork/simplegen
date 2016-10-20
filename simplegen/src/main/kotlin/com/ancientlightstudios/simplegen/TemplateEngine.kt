package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.filters.CaseFilter
import com.ancientlightstudios.simplegen.filters.JsonPathFilter
import com.ancientlightstudios.simplegen.filters.SystemPropertyFilter
import com.hubspot.jinjava.Jinjava
import org.slf4j.LoggerFactory

object TemplateEngine {
    private val log = LoggerFactory.getLogger(Runner::class.java)

    fun execute(input: String, node: Any, data: Any, basePath: String): String {
        return execute(input, mapOf("data" to data, "node" to node), listOf(basePath))
    }

    fun execute(input: String, context: Map<String, Any>, templateBasePaths: List<String> = listOf(".")): String {
        if (log.isDebugEnabled) {
            log.debug("Template: $input")
            log.debug("Context: $context")
        }

        val templateEngine = Jinjava()
        templateEngine.resourceLocator = RepositoryFileLocator(templateBasePaths)
        templateEngine.globalContext.registerFilter(JsonPathFilter())
        templateEngine.globalContext.registerFilter(SystemPropertyFilter())
        templateEngine.globalContext.registerFilter(CaseFilter())
        return templateEngine.render(input, context)
    }
}