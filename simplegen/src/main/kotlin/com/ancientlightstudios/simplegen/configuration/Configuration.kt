package com.ancientlightstudios.simplegen.configuration

class Configuration(
        val transformations: List<Transformation> = emptyList(),
        val templateEngine: TemplateEngineConfiguration = TemplateEngineConfiguration(),
        val customFilters: List<CustomFilterConfiguration> = emptyList(),
        var lastModified: Long = 0)
