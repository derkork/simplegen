package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.resources.FileNotResolvedException
import com.ancientlightstudios.simplegen.resources.FileUtil
import com.ancientlightstudios.simplegen.resources.SimpleFileResolver
import com.hubspot.jinjava.lib.filter.Filter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.script.ScriptException


class Runner(private val basePath: String = ".",
             private val configPath: String = "config.yml",
             private val outputFolder: String = ".",
             private val forceUpdate: Boolean = false) {

    private val log: Logger = LoggerFactory.getLogger(Runner::class.java)

    fun run(): Boolean {
        val startTime = System.currentTimeMillis()

        val fileResolver = SimpleFileResolver(basePath)

        log.info("Reading configuration...")

        val configFile = try {
            FileUtil.resolve(basePath, configPath)
        } catch (e: FileNotResolvedException) {
            log.error("No configuration file '$configPath' present in '$basePath'.")
            return false
        }

        if (!configFile.exists()) {
            log.error("No configuration file '$configPath' present in '$basePath'.")
            return false
        }

        try {

            val config = configFile.inputStream().use {
                ConfigurationReader.readConfiguration(it, configFile.path)
            }

            val materializer = Materializer(fileResolver)
            val materializedTransformations = materializer.materialize(config)
            val filters = config.customFilters.flatMap { FilterBuilder.buildFilter(it, fileResolver) }


            log.info("Processing ${materializedTransformations.size} transformations...")

            val (rendered, upToDate) = renderTemplates(filters, materializedTransformations, fileResolver)

            val endTime = System.currentTimeMillis()
            val totalTime = endTime - startTime
            log.info("Generation complete ($rendered rendered/$upToDate up-to-date) in ${totalTime}ms.")
            return true
        } catch (e: ScriptException) {
            handle(e)
        } catch (e: FileNotResolvedException) {
            handle(e)
        } catch (e: TemplateErrorException) {
            handle(e)
        } catch (e: ConfigurationException) {
            handle(e)
        } catch( e: DataParseException) {
            handle(e)
        } catch (e: Exception) {
            handle(e)
        }
        return false
    }

    private fun renderTemplates(filters: List<DependencyObject<Filter>>,
                                materializedTransformations: List<DependencyObject<MaterializedTransformation>>,
                                fileResolver: SimpleFileResolver): Pair<Int, Int> {

        val globalFiltersLastModified = filters.lastModified()
        val globalFilters = filters.objects()

        var rendered = 0
        var upToDate = 0

        for ((item, lastModified) in materializedTransformations) {
            val outputFile = FileUtil.resolve(outputFolder, item.outputPath)

            if (!forceUpdate) {
                val outputLastModified = outputFile.lastModified()
                if (outputFile.exists() &&
                        outputLastModified >= lastModified &&
                        outputLastModified >= globalFiltersLastModified) {
                    upToDate++
                    log.debug("Output file ${item.outputPath} is up-to-date not rendering it again.")
                    continue
                }
            }

            rendered++

            log.debug("Rendering ${item.templateSource} into ${item.outputPath}.")
            val parent = outputFile.parentFile
            if (!parent.exists()) {
                parent.mkdirs()
            }

            val engineArguments = TemplateEngineArguments(item.templateEngineConfiguration, globalFilters)
            val templateEngine = TemplateEngine(fileResolver, engineArguments)
            val templateEngineJob = TemplateEngineJob(item.templateSource, item.template)
                    .with(item.data, item.node)

            val result = templateEngine.execute(templateEngineJob)
            outputFile.writeText(result)
        }

        return Pair(rendered, upToDate)
    }

    private fun handle(e: TemplateErrorException) {
        log.error("When rendering template: '${e.job.source}'")
        e.result.errors.forEach {
            log.error(" @ line: ${it.lineno} - ${it.message} ")
        }
    }

    private fun handle(e: ConfigurationException) {
        log.error("When parsing configuration YAML file: '${e.origin}'")
        log.error(" ${e.message}")
    }

    private fun handle(e: DataParseException) {
        log.error("When parsing data file: '${e.origin}'")
        log.error(" ${e.message}")
    }

    private fun handle(e: FileNotResolvedException) {
        log.error("The file ${e.message} referenced in your configuration cannot be found.")
    }

    private fun handle(e: ScriptException) {
        log.error("When evaluating script: ${e.message}")
    }

    private fun handle(e: Exception) {
        log.error("Unexpected exception: ${e.message} ", e)
    }

}