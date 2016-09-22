package com.ancientlightstudios.simplegen

import com.jayway.jsonpath.JsonPath
import org.slf4j.LoggerFactory


class Materializer(val basePath: String) {

    val log = LoggerFactory.getLogger(Materializer::class.java)

    fun materialize(source: Configuration): List<MaterializedTransformation> = source.transformations.flatMap { materialize(it) }

    private fun materialize(source: Configuration.Transformation): List<MaterializedTransformation> {
        val templateText = FileUtil.resolve(basePath, source.template).readText()
        val dataMaps = source.getParsedData()
                .flatMap { FileUtil.resolve(FileUtil.resolve(basePath, it.baseDir).absolutePath, it.includes, it.excludes) }
                .map { it.inputStream().use { YamlReader.readToMap(it) } }
                .toTypedArray()

        if (log.isDebugEnabled) {
            log.debug("Merging data from ${dataMaps.size} source files.")
            for (dataMap in dataMaps) {
                log.info("Source: $dataMap")
            }
        }
        val data = JsonUtil.merge(*dataMaps)

        if (log.isDebugEnabled) {
            log.debug("Result: $data")
        }

        var nodes = JsonPath.read<Any>(data, source.nodes)
        if (nodes !is Iterable<*>) {
            nodes = listOf<Any>(nodes)
        }

        val result = mutableListOf<MaterializedTransformation>()
        for (node in nodes as Iterable<*>) {
            val outputFile = TemplateEngine.execute(source.outputPath, node!!, data, basePath)
            result.add(MaterializedTransformation(templateText, data, node, outputFile))
        }
        return result
    }


}