package com.ancientlightstudios.simplegen.configuration

class Transformation(
    val template: String = "",
    val data: Any = emptyList<Any>(),
    val nodes: Any = "",
    val outputPath: String = "",
    val templateEngine: TemplateEngineConfiguration? = null
) {

    val parsedData: List<DataSpec> by lazy { parseData() }

    val parsedNodesExpression: NodesSpec by lazy { parseNodes() }

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
                val resultPath = item["resultPath"] as? String? ?: ""
                @Suppress("UNCHECKED_CAST")
                val parserSettings = item["parserSettings"] as? Map<String, Any> ?: mapOf()

                list.add(DataSpec(baseDir ?: "", includes, excludes, mimeType, inlineData, null, resultPath, parserSettings))
            }
        }
        return list
    }

    private fun parseNodes(): NodesSpec {
        // if nodes is just a string, we assume it to be a jsonpath expression,
        // if it is a map, we extract the expression and the type from the map
        return when (nodes) {
            is String -> NodesSpec(nodes as String, "jsonpath")
            is Map<*, *> -> {
                val expression = nodes["expression"] as? String ?: ""
                val type = nodes["type"] as? String ?: "jsonpath"
                NodesSpec(expression, type)
            }
            else -> NodesSpec("", "")
        }
    }

    class DataSpec(
        val basePath: String,
        val includes: List<String>,
        val excludes: List<String>,
        val mimeType: String?,
        val inlineData: Any?,
        val file: String? = null,
        val resultPath: String = "",
        val parserSettings: Map<String, Any> = emptyMap(),
    )

    class NodesSpec(
        val expression:String,
        val type: String
    )
}