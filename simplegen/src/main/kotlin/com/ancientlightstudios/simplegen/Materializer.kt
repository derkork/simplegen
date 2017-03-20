package com.ancientlightstudios.simplegen

import com.jayway.jsonpath.JsonPath
import org.slf4j.LoggerFactory


class Materializer(val basePath: String) {

    val log = LoggerFactory.getLogger(Materializer::class.java)

    fun materialize(source: Configuration): List<MaterializedTransformation> = source.transformations.flatMap { materialize(source, it) }

    private fun materialize(configuration: Configuration, source: Configuration.Transformation): List<MaterializedTransformation> {
        log.debug("Reading data from source files.")

        val dataMaps = source.getParsedData()
                .flatMap {
                    // we template all input now.
                    val baseDir = TemplateEngine.execute(it.basePath, emptyMap(), listOf(basePath))
                    val includes = it.includes.map { TemplateEngine.execute(it, emptyMap(), listOf(basePath)) }
                    val excludes = it.excludes.map { TemplateEngine.execute(it, emptyMap(), listOf(basePath)) }
                    FileUtil.resolve(
                            FileUtil.resolve(basePath, baseDir).canonicalPath, includes, excludes)
                }
                .map { it.inputStream().use { YamlReader.readToMap(it) } }
                .toTypedArray()

        val data: Map<String, Any>
        if (dataMaps.isEmpty()) {
            log.warn("Your input files didn't yield any data. Please check if the file names are correct.")
            data = emptyMap()
        } else {
            if (log.isDebugEnabled) {
                log.debug("Merging data from ${dataMaps.size} source files.")
                for (dataMap in dataMaps) {
                    log.info("Source: $dataMap")
                }
            }
            data = JsonUtil.merge(*dataMaps)
        }

        if (log.isDebugEnabled) {
            log.debug("Result: $data")
        }

        var nodes = JsonPath.read<Any>(data, source.nodes)

        if (nodes !is Iterable<*>) {
            nodes = listOf<Any>(nodes)
        }

        val result = mutableListOf<MaterializedTransformation>()
        for (node in nodes as Iterable<*>) {

            node ?: throw IllegalStateException("Unexpected null node.")

            val outputFile = TemplateEngine.execute(source.outputPath, node, data, basePath)
            val templateSource = TemplateEngine.execute(source.template, node, data, basePath)
            val templateText = FileUtil.resolve(basePath, templateSource).readText()


            var engineConfiguration = source.templateEngine
            if (engineConfiguration == null) {
                engineConfiguration = configuration.templateEngine
            }
            result.add(MaterializedTransformation(templateText, data, node, outputFile, engineConfiguration))
        }
        return result
    }


}