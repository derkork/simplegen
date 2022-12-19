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
import java.io.File
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
                    log.warn("Inline data specified in your ${configuration.origin} needs to be a map. Ignoring this data.")
                }
            } else {
                dataMaps.addAll(listOf(item).flatMap { dataSpec ->
                    // a single file was given as data source, this can be a relative path.
                    if (dataSpec.file != null) {
                        val parsedFile = templateEngine.execute(
                            TemplateEngineJob(
                                "${configuration.origin} -> transformations -> data",
                                dataSpec.file
                            )
                        )
                        try {
                            val resolvedFile = fileResolver.resolve(parsedFile)
                            return@flatMap listOf(MaterializedFile(resolvedFile, null, dataSpec.resultPath, mapOf<String,Any>()))
                        }
                        catch(e:FileNotResolvedException) {
                            log.warn("Data file '${parsedFile}' could not be resolved. Ignoring this file.")
                        }
                    }

                    // we template all input now.
                    val baseDir = templateEngine.execute(
                        TemplateEngineJob(
                            "${configuration.origin} -> transformations -> data -> basePath",
                            dataSpec.basePath
                        )
                    )

                    val includes = dataSpec.includes.map { file ->
                        templateEngine.execute(
                            TemplateEngineJob(
                                "${configuration.origin} -> transformations -> data -> includes",
                                file
                            )
                        )
                    }

                    val excludes = dataSpec.excludes.map { file ->
                        templateEngine.execute(
                            TemplateEngineJob(
                                "${configuration.origin} -> transformations -> data -> excludes",
                                file
                            )
                        )
                    }

                    val mimeType = when {
                        dataSpec.mimeType != null -> templateEngine.execute(
                            TemplateEngineJob(
                                "${configuration.origin} -> transformations -> data -> mimeType",
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

                    resolvedFiles.map { file -> MaterializedFile(file, mimeType, dataSpec.resultPath, dataSpec.parserSettings) }
                }
                    .map { file ->
                        DependencyObject(DataLoader.parse(file.file, file.mimeType, file.resultPath, file.options), file.file.lastModified())
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
        val nodesSpec = source.parsedNodesExpression

        var nodes = when (nodesSpec.type){
            "", "jsonpath" -> evaluateJsonPath(configuration.origin, nodesSpec.expression, data)
            "jinja2" -> evaluateJinja2(configuration.origin, templateEngine, nodesSpec.expression, data)
            else -> throw ConfigurationException("${configuration.origin} -> transformations -> nodes -> type",
                "unknown node expression type: ${nodesSpec.type}")
        }

        if (nodes == null) {
            log.warn("In ${configuration.origin} -> transformations -> nodes: No nodes found for expression '${nodesSpec.expression}'.")
            nodes = emptyList<Any>()
        }

        if (nodes !is Iterable<*>) {
            nodes = listOf(nodes)
        }

        val result = mutableListOf<DependencyObject<MaterializedTransformation>>()
        for (node in nodes as Iterable<*>) {

            node ?: throw IllegalStateException("Unexpected null node.")

            val outputFile = templateEngine.execute(
                TemplateEngineJob(
                    "${configuration.origin} -> transformations -> outputPath",
                    source.outputPath
                ).with(data, node)
            )
            val templateSource = templateEngine.execute(
                TemplateEngineJob(
                    "${configuration.origin} -> transformations -> template",
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

    private fun evaluateJinja2(origin: String, templateEngine: TemplateEngine,
                               expression: String, data: Map<String, Any>): Any? {
        try {
            return templateEngine.evaluateExpression(expression, data)
        } catch (e: Exception) {
            throw ConfigurationException("$origin -> transformations -> nodes -> expression", e.message ?: "unknown error")
        }
    }


    private fun evaluateJsonPath(origin: String, jsonPath: String, data: Map<String, Any>): Any? {
        var nodePath = jsonPath
        if (nodePath.isEmpty()) {
            log.warn("No nodes specified transformation. Assuming root node ($).")
            nodePath = "$"
        }

        return try {
            JsonPath.read<Any?>(data, nodePath)
        } catch (e: Exception) {
            throw ConfigurationException(
                "$origin -> transformations -> nodes",
                "Invalid JSONPath: $nodePath"
            )
        }
    }
    class MaterializedFile(
        val file: File,
        val mimeType: String?,
        val resultPath: String,
        val options: Map<String, Any>
    )


}