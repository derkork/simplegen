package com.ancientlightstudios.simplegen.configuration

class Transformation(
    val template: String = "",
    val data: Any = emptyList<Any>(),
    val nodes: String = "",
    val outputPath: String = "",
    val templateEngine: TemplateEngineConfiguration? = null
) {

    val parsedData: List<DataSpec> by lazy { parseData() }

    private fun parseData(): List<DataSpec> {
        val list = mutableListOf<DataSpec>()

        val input = data as? List<*> ?: listOf(data)

        for (item in input) {
            if (item is String) {
                list.add(DataSpec("", listOf(), listOf(), null, null, item))
            }

            if (item is Map<*, *>) {
                val baseDir = item["basePath"] as String?
                val includes = item["includes"].asStringList()
                val excludes = item["excludes"].asStringList()
                val mimeType = item["mimeType"] as String?
                val inlineData = item["inline"]
                @Suppress("UNCHECKED_CAST")
                val parserSettings = item["parserSettings"] as? Map<String, Any> ?: mapOf()

                list.add(DataSpec(baseDir ?: "", includes, excludes, mimeType, inlineData, null, parserSettings))
            }
        }
        return list
    }

    class DataSpec(
        val basePath: String,
        val includes: List<String>,
        val excludes: List<String>,
        val mimeType: String?,
        val inlineData: Any?,
        val file: String? = null,
        val parserSettings: Map<String, Any> = emptyMap(),
    )
}