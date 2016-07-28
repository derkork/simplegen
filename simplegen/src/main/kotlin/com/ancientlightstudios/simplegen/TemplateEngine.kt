package com.ancientlightstudios.simplegen

import com.hubspot.jinjava.Jinjava

object TemplateEngine {

    fun execute(input:String, context:Map<String,Any>, templateBasePaths:List<String> = listOf(".")) : String {
        val templateEngine = Jinjava()
        templateEngine.resourceLocator = RepositoryFileLocator(templateBasePaths)
        templateEngine.globalContext.registerFilter(JsonPathFilter())
        return templateEngine.render(input, context)
    }
}