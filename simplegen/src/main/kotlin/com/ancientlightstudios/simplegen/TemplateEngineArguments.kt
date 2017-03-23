package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.configuration.TemplateEngineConfiguration
import com.hubspot.jinjava.lib.filter.Filter

class TemplateEngineArguments(var configuration: TemplateEngineConfiguration = TemplateEngineConfiguration(),
                              var additionalFilters:List<Filter> = emptyList())
