package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.filters.CaseFilter
import com.ancientlightstudios.simplegen.filters.EnvironmentVariableFilter
import com.ancientlightstudios.simplegen.filters.JsonPathFilter
import com.ancientlightstudios.simplegen.filters.SystemPropertyFilter
import com.ancientlightstudios.simplegen.resources.FileResolver
import com.hubspot.jinjava.Jinjava
import com.hubspot.jinjava.JinjavaConfig
import com.hubspot.jinjava.interpret.Context

class TemplateEngine(fileResolver: FileResolver, arguments: TemplateEngineArguments = TemplateEngineArguments()) {

    private val templateEngine : Jinjava

    init {
        val configBuilder = JinjavaConfig.newBuilder()
        configBuilder.withLstripBlocks(arguments.configuration.lstripBlocks)
        configBuilder.withTrimBlocks(arguments.configuration.trimBlocks)
        configBuilder.withEnableRecursiveMacroCalls(arguments.configuration.enableRecursiveMacroCalls)
        configBuilder.withNestedInterpretationEnabled(arguments.configuration.nestedInterpretationEnabled)

        templateEngine = Jinjava(configBuilder.build())
        templateEngine.resourceLocator = RepositoryFileLocator(fileResolver)
        templateEngine.globalContext.registerFilter(JsonPathFilter())
        templateEngine.globalContext.registerFilter(SystemPropertyFilter())
        templateEngine.globalContext.registerFilter(CaseFilter())
        templateEngine.globalContext.registerFilter(EnvironmentVariableFilter())

        arguments.additionalFilters.forEach { additionalFilter -> templateEngine.globalContext.registerFilter(additionalFilter) }
    }


    fun execute(job: TemplateEngineJob): String {
        val result = templateEngine.renderForResult(job.template, job.context)
        if (result.hasErrors()) {
            throw TemplateErrorException(job, result)
        }
        return result.output
    }

    fun evaluateExpression(expression: String, bindings: Map<String, Any>): Any? {
        val context = Context(
            templateEngine.globalContext,
            mapOf("data" to bindings),
        )
        val interpreter = templateEngine.globalConfig.interpreterFactory
            .newInstance(templateEngine,context,templateEngine.globalConfig)

        return interpreter.resolveELExpression(expression, 0)
    }
}