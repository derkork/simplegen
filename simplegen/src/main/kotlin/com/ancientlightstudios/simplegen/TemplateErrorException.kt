package com.ancientlightstudios.simplegen

import com.hubspot.jinjava.interpret.RenderResult

class TemplateErrorException(val job:TemplateEngineJob, val result:RenderResult) : RuntimeException(result.errors.joinToString { it.toString() })
