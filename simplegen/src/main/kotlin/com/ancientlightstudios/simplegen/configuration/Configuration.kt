package com.ancientlightstudios.simplegen.configuration

class Configuration(
        val transformations: List<Transformation> = emptyList(),
        val templateEngine: TemplateEngineConfiguration = TemplateEngineConfiguration(),
        val customFilters: List<CustomFilterConfiguration> = emptyList(),
        val extensions: Map<String, Any> = emptyMap(),
        var lastModified: Long = 0,
        var origin: String = "")
