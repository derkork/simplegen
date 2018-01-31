package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.configuration.Configuration
import com.ancientlightstudios.simplegen.configuration.TemplateEngineConfiguration
import com.ancientlightstudios.simplegen.configuration.Transformation
import com.ancientlightstudios.simplegen.resources.FileResolver
import com.ancientlightstudios.simplegen.resources.SimpleFileResolver
import com.jayway.jsonpath.JsonPath
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class Materializer(private val fileResolver: FileResolver) {

    private val log: Logger = LoggerFactory.getLogger(Materializer::class.java)

    fun materialize(source: Configuration): List<MaterializedTransformation> = source.transformations.flatMap { materialize(source, it) }

    private fun materialize(configuration: Configuration, source: Transformation): List<MaterializedTransformation> {

        val filters = configuration.customFilters.map { FilterBuilder.buildFilter(it, fileResolver) }
        val templateEngineArguments = TemplateEngineArguments(TemplateEngineConfiguration(), filters)
        val templateEngine = TemplateEngine(fileResolver, templateEngineArguments)

        log.debug("Reading data from source files.")

        val dataMaps = source.getParsedData()
                .flatMap {
                    // we template all input now.
                    val baseDir = templateEngine.execute(TemplateEngineJob("config.yml -> transformations -> data -> basePath",  it.basePath))
                    val includes = it.includes.map { templateEngine.execute(TemplateEngineJob("config.yml -> transformations -> data -> includes",it)) }
                    val excludes = it.excludes.map { templateEngine.execute(TemplateEngineJob("config.yml -> transformations -> data -> excludes", it)) }

                    val baseDirAsFile = fileResolver.resolve(baseDir)
                    val customResolver = SimpleFileResolver(baseDirAsFile.canonicalPath)
                    customResolver.resolve(includes, excludes)
                }
                .map { val file = it; file.inputStream().use { YamlReader.readToMap(file.path, it) } }
                .toTypedArray()

        val data: Map<String, Any>
        data = if (dataMaps.isEmpty()) {
            log.warn("Your input files didn't yield any data. Please check if the file names are correct.")
            emptyMap()
        } else {
            if (log.isDebugEnabled) {
                log.debug("Merging data from ${dataMaps.size} source files.")
                for (dataMap in dataMaps) {
                    log.debug("Source: $dataMap")
                }
            }
            JsonUtil.merge(*dataMaps)
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

            val outputFile = templateEngine.execute(TemplateEngineJob("config.yml -> transformations -> outputPath", source.outputPath).with( data, node))
            val templateSource = templateEngine.execute(TemplateEngineJob( "config.yml -> transformations -> template", source.template).with(data, node))
            val templateText =  fileResolver.resolve(templateSource).readText()

            val engineConfiguration = source.templateEngine ?: configuration.templateEngine
            result.add(MaterializedTransformation(templateSource, templateText, data, node, outputFile, engineConfiguration))
        }
        return result
    }


}