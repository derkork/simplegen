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

    fun materialize(source: Configuration): List<DependencyObject<MaterializedTransformation>> = source.transformations.flatMap { materialize(source, it) }

    private fun materialize(configuration: Configuration, source: Transformation): List<DependencyObject<MaterializedTransformation>> {

        val filters = configuration.customFilters.map { FilterBuilder.buildFilter(it, fileResolver) }
        val filtersLastModified = filters.lastModified()
        val templateEngineArguments = TemplateEngineArguments(TemplateEngineConfiguration(), filters.map { it.item })
        val templateEngine = TemplateEngine(fileResolver, templateEngineArguments)

        log.debug("Reading data from source files.")

        val dataMaps = source.getParsedData()
                .flatMap {
                    // we template all input now.
                    val baseDir = templateEngine.execute(TemplateEngineJob("config.yml -> transformations -> data -> basePath", it.basePath))
                    val includes = it.includes.map { file -> templateEngine.execute(TemplateEngineJob("config.yml -> transformations -> data -> includes", file)) }
                    val excludes = it.excludes.map { file -> templateEngine.execute(TemplateEngineJob("config.yml -> transformations -> data -> excludes", file)) }

                    val baseDirAsFile = fileResolver.resolve(baseDir)
                    val customResolver = SimpleFileResolver(baseDirAsFile.canonicalPath)
                    customResolver.resolve(includes, excludes)
                }
                .map { val file = it; DependencyObject(file.inputStream().use { stream -> YamlReader.readToMap(file.path, stream) }, file.lastModified()) }
                .toList()

        val dataLastModified: Long = dataMaps.lastModified()
        val data: Map<String, Any> = if (dataMaps.isEmpty()) {
            log.warn("Your input files didn't yield any data. Please check if the file names are correct.")
            emptyMap()
        } else {
            if (log.isDebugEnabled) {
                log.debug("Merging data from ${dataMaps.size} source files.")
            }
            JsonUtil.merge(*dataMaps.objects().toTypedArray())
        }

        if (log.isDebugEnabled) {
            log.debug("Result: $data")
        }

        var nodes = JsonPath.read<Any>(data, source.nodes)

        if (nodes !is Iterable<*>) {
            nodes = listOf<Any>(nodes)
        }

        val result = mutableListOf<DependencyObject<MaterializedTransformation>>()
        for (node in nodes as Iterable<*>) {

            node ?: throw IllegalStateException("Unexpected null node.")

            val outputFile = templateEngine.execute(TemplateEngineJob("config.yml -> transformations -> outputPath", source.outputPath).with(data, node))
            val templateSource = templateEngine.execute(TemplateEngineJob("config.yml -> transformations -> template", source.template).with(data, node))
            val templateFile = fileResolver.resolve(templateSource)
            val templateText = templateFile.readText()
            val lastModified = Math.max(filtersLastModified, Math.max(templateFile.lastModified(), dataLastModified))

            val engineConfiguration = source.templateEngine ?: configuration.templateEngine
            result.add(
                    DependencyObject(
                            MaterializedTransformation(templateSource, templateText, data, node, outputFile, engineConfiguration),
                            lastModified
                    )
            )
        }
        return result
    }


}