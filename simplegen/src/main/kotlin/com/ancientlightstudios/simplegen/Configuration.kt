package com.ancientlightstudios.simplegen


data class Configuration(val transformations: List<Transformation> = emptyList<Transformation>()) {
    data class Transformation(val template: String = "", val data: List<String> = emptyList(), val nodes: String = "", val outputPath: String = "")
}
