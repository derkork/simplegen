package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.configuration.TemplateEngineConfiguration


/**
 * This is a materialized transformation that contains everything needed to actually perform the transformation.
 */
data class MaterializedTransformation(
        val templateSource: String,
        val template: String,
        val data: Map<String, Any>,
        val node: Any,
        val outputPath: String,
        val templateEngineConfiguration: TemplateEngineConfiguration)