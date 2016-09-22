package com.ancientlightstudios.simplegen


class Configuration(val transformations: List<Transformation> = emptyList<Transformation>()) {
    class Transformation(val template: String = "", val data: Any = emptyList<Any>(), val nodes: String = "", val outputPath: String = "") {

        private var parsedData: List<DataSpec>? = null

        fun getParsedData(): List<DataSpec> {
            synchronized(this) {
                if (parsedData != null) {
                    return parsedData as List<DataSpec>
                }


                val list = mutableListOf<DataSpec>()

                val input = if (data is List<*>) data else listOf(data)

                for (item in input) {
                    if (item is String) {
                        list.add(DataSpec("", listOf(item), listOf()))
                    }

                    if (item is Map<*, *>) {
                        val baseDir = item["base_dir"] as String?
                        val includes = asList(item["includes"])
                        val excludes = asList(item["excludes"])

                        list.add(DataSpec(baseDir ?: "", includes, excludes))
                    }
                }

                parsedData = list
                return parsedData as List<DataSpec>
            }

        }

        private fun asList(any: Any?): List<String> {
            if (any is String) {
                return listOf(any)
            }
            if (any is List<*>) {
                @Suppress("UNCHECKED_CAST")
                return any as List<String>
            }
            if (any != null) {
                return listOf(any.toString())
            }
            return emptyList()
        }

        class DataSpec(val baseDir: String, val includes: List<String>, val excludes: List<String>) {
        }
    }
}
