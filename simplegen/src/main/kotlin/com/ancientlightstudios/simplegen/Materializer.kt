package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.configuration.Configuration
import com.ancientlightstudios.simplegen.configuration.TemplateEngineConfiguration
import com.ancientlightstudios.simplegen.configuration.Transformation
import com.ancientlightstudios.simplegen.resources.FileNotResolvedException
import com.ancientlightstudios.simplegen.resources.FileResolver
import com.ancientlightstudios.simplegen.resources.SimpleFileResolver
import com.jayway.jsonpath.JsonPath
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.max

/**
 * Responsible for materializing the configuration into a list of transformations to be executed.
 */
class Materializer(private val fileResolver: FileResolver) {

    private val log: Logger = LoggerFactory.getLogger(Materializer::class.java)

    fun materialize(source: Configuration): List<DependencyObject<MaterializedTransformation>> =
        source.transformations.flatMap { materialize(source, it) }

    private fun materialize(configuration: Configuration, source: Transformation):
            List<DependencyObject<MaterializedTransformation>> {

        val filters = configuration.customFilters.flatMap { FilterBuilder.buildFilter(it, fileResolver) }
        val filtersLastModified = filters.lastModified()
        val templateEngineArguments = TemplateEngineArguments(TemplateEngineConfiguration(), filters.map { it.item })
        val templateEngine = TemplateEngine(fileResolver, templateEngineArguments)

        DataLoader.initParsers(configuration.extensions)

        log.debug("Reading data from source files.")

        val dataMaps = mutableListOf<DependencyObject<Map<String, Any>>>()

        for (item in source.parsedData) {
            if (item.inlineData != null) {
                if (item.inlineData is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    dataMaps.add(DependencyObject(item.inlineData as Map<String, Any>, configuration.lastModified))
                } else {
                    log.warn("Inline data specified in your config.yml needs to be a map. Ignoring this data.")
                }
            } else {
                dataMaps.addAll(listOf(item).flatMap { dataSpec ->
                    // a single file was given as data source, this can be a relative path.
                    if (dataSpec.file != null) {
                        val parsedFile = templateEngine.execute(
                            TemplateEngineJob(
                                "config.yml -> transformations -> data",
                                dataSpec.file
                            )
                        )
                        try {
                            val resolvedFile = fileResolver.resolve(parsedFile)
                            return@flatMap listOf(Triple(resolvedFile, null, mapOf<String,Any>()))
                        }
                        catch(e:FileNotResolvedException) {
                            log.warn("Data file '${parsedFile}' could not be resolved. Ignoring this file.")
                        }
                    }

                    // we template all input now.
                    val baseDir = templateEngine.execute(
                        TemplateEngineJob(
                            "config.yml -> transformations -> data -> basePath",
                            dataSpec.basePath
                        )
                    )

                    val includes = dataSpec.includes.map { file ->
                        templateEngine.execute(
                            TemplateEngineJob(
                                "config.yml -> transformations -> data -> includes",
                                file
                            )
                        )
                    }

                    val excludes = dataSpec.excludes.map { file ->
                        templateEngine.execute(
                            TemplateEngineJob(
                                "config.yml -> transformations -> data -> excludes",
                                file
                            )
                        )
                    }

                    val mimeType = when {
                        dataSpec.mimeType != null -> templateEngine.execute(
                            TemplateEngineJob(
                                "config.yml -> transformations -> data -> mimeType",
                                dataSpec.mimeType
                            )
                        )
                        else -> null
                    }

                    val baseDirAsFile = fileResolver.resolve(baseDir)
                    val customResolver = SimpleFileResolver(baseDirAsFile.canonicalPath)
                    val resolvedFiles = customResolver.resolve(includes, excludes)
                    if (resolvedFiles.isEmpty()) {
                        log.warn("No data files could be resolved for basePath '${dataSpec.basePath}', " +
                                "includes '${dataSpec.includes.joinToString(",") }," +
                                "excludes '${dataSpec.excludes.joinToString("")} . Ignoring this data source.")
                    }

                    resolvedFiles.map { file -> Triple(file, mimeType, dataSpec.parserSettings) }
                }
                    .map { triple ->
                        DependencyObject(DataLoader.parse(triple.first, triple.third, triple.second), triple.first.lastModified())
                    }
                    .toList())

            }

        }


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

        // if nodes is not specified, we use the root node
        var nodePath = source.nodes
        if (nodePath.isEmpty()) {
            log.warn("No nodes specified transformation. Assuming root node ($).")
            nodePath = "$"
        }

        var nodes = try {
            JsonPath.read<Any>(data, nodePath)
        } catch (e: Exception) {
            throw ConfigurationException("config.yml -> transformations -> nodes", "Invalid JSONPath: ${source.nodes}")
        }

        if (nodes !is Iterable<*>) {
            nodes = listOf<Any>(nodes)
        }

        val result = mutableListOf<DependencyObject<MaterializedTransformation>>()
        for (node in nodes as Iterable<*>) {

            node ?: throw IllegalStateException("Unexpected null node.")

            val outputFile = templateEngine.execute(
                TemplateEngineJob(
                    "config.yml -> transformations -> outputPath",
                    source.outputPath
                ).with(data, node)
            )
            val templateSource = templateEngine.execute(
                TemplateEngineJob(
                    "config.yml -> transformations -> template",
                    source.template
                ).with(data, node)
            )
            val templateFile = fileResolver.resolve(templateSource)
            val templateText = templateFile.readText()
            val lastModified = max(filtersLastModified, max(templateFile.lastModified(), dataLastModified))

            val engineConfiguration = source.templateEngine ?: configuration.templateEngine
            result.add(
                DependencyObject(
                    MaterializedTransformation(
                        templateSource,
                        templateText,
                        data,
                        node,
                        outputFile,
                        engineConfiguration
                    ),
                    lastModified
                )
            )
        }
        return result
    }


}