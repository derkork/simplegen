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
                list.add(DataSpec("", listOf(item), listOf(), null, null))
            }

            if (item is Map<*, *>) {
                val baseDir = item["basePath"] as String?
                val includes = item["includes"].asStringList()
                val excludes = item["excludes"].asStringList()
                val mimeType = item["mimeType"] as String?
                val inlineData = item["inline"]

                list.add(DataSpec(baseDir ?: "", includes, excludes, mimeType, inlineData))
            }
        }
        return list
    }

    class DataSpec(
        val basePath: String,
        val includes: List<String>,
        val excludes: List<String>,
        val mimeType: String?,
        val inlineData: Any?
    )
}