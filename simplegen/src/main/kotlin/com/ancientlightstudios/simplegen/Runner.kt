package com.ancientlightstudios.simplegen

import com.jayway.jsonpath.JsonPath


class Runner(private val basePath: String = ".", configPath:String = "config.yml", private val outputFolder:String = ".") {
    private lateinit var config: Configuration

    init {
        val configFile = FileUtil.resolve(basePath, configPath)
        if (!configFile.exists()) {
            throw IllegalStateException("No configuration file config.yml present.")
        }
        configFile.inputStream().use {
            config = YamlReader.readToPojo(it, Configuration::class.java)
        }
    }


    fun run() {
        val materializedTransformations = config.transformations.flatMap { materialize(it) }
        for ((template, data, node, outputPath) in materializedTransformations) {
            val outputFile = FileUtil.resolve(outputFolder, outputPath)
            val parent = outputFile.parentFile
            if (!parent.exists()) {
                parent.mkdirs()
            }

            val result = runTemplateEngine(template, data, node)
            outputFile.writeText(result)
        }
    }

    private fun materialize(source: Configuration.Transformation): List<MaterializedTransformation> {

        val templateText = FileUtil.resolve(basePath, source.template).readText()
        val data = JsonUtil.merge(*source.data.map { FileUtil.resolve(basePath, it).inputStream().use { YamlReader.readToMap(it) } }.toTypedArray())

        var nodes = JsonPath.read<Any>(data, source.nodes)
        if (nodes !is Iterable<*>) {
            nodes = listOf<Any>(nodes)
        }

        val result = mutableListOf<MaterializedTransformation>()
        for (node in nodes as Iterable<*>) {
            val outputFile = runTemplateEngine(source.outputPath, data, node!!)
            result.add(MaterializedTransformation(templateText, data, node, outputFile ))
        }

        return result

    }

    private fun runTemplateEngine(input: String, data: Any, node: Any): String {
        return TemplateEngine.execute(input, mapOf("data" to data, "node" to node), listOf(basePath))
    }

}