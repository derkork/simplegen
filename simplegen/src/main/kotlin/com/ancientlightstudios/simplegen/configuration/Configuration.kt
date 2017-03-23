package com.ancientlightstudios.simplegen.configuration

class Configuration(
        val transformations: List<Transformation> = emptyList<Transformation>(),
        val templateEngine: TemplateEngineConfiguration = TemplateEngineConfiguration(),
        val customFilters: List<CustomFilterConfiguration> = emptyList<CustomFilterConfiguration>())
