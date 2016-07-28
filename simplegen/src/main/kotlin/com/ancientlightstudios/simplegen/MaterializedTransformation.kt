package com.ancientlightstudios.simplegen


/**
 * This is a materialized transformation that contains everything needed to actually perform the transformation.
 */
data class MaterializedTransformation(var template: String, var data: Map<String, Any>, var node: Any, var outputPath: String)