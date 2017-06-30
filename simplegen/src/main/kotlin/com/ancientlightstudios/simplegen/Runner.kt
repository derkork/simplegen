package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.configuration.Configuration
import com.ancientlightstudios.simplegen.resources.FileNotResolvedException
import com.ancientlightstudios.simplegen.resources.FileUtil
import com.ancientlightstudios.simplegen.resources.SimpleFileResolver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File


class Runner(val basePath: String = ".", val configPath: String = "config.yml", private val outputFolder: String = ".") {

    private val log: Logger = LoggerFactory.getLogger(Runner::class.java)

    fun run(): Boolean {
        val fileResolver = SimpleFileResolver(basePath)

        log.info("Reading configuration...")
        val configFile: File
        try {
            configFile = FileUtil.resolve(basePath, configPath)
        } catch(e: FileNotResolvedException) {
            log.error("No configuration file config.yml present.")
            return false
        }

        try {

            val config = configFile.inputStream().use {
                YamlReader.readToPojo(configFile.path, it, Configuration::class.java)
            }

            val materializer = Materializer(fileResolver)

            val materializedTransformations = materializer.materialize(config)
            val filters = config.customFilters.map { FilterBuilder.buildFilter(it, fileResolver) }

            log.info("Processing ${materializedTransformations.size} transformations...")
            for ((templateSource, template, data, node, outputPath, engineConfiguration) in materializedTransformations) {
                log.debug("Rendering $templateSource into $outputPath.")
                val outputFile = FileUtil.resolve(outputFolder, outputPath)
                val parent = outputFile.parentFile
                if (!parent.exists()) {
                    parent.mkdirs()
                }

                val engineArguments = TemplateEngineArguments(engineConfiguration, filters)
                val templateEngine = TemplateEngine(fileResolver, engineArguments)

                val result = templateEngine.execute(TemplateEngineJob(templateSource, template).with(data, node))
                outputFile.writeText(result)
            }

            return true
        } catch(e: TemplateErrorException) {
            handle(e)
        } catch(e: YamlErrorException) {
            handle(e)
        } catch(e: Exception) {
            handle(e)
        }
        return false
    }

    private fun handle(e: TemplateErrorException) {
        log.error("ERROR when rendering template: '${e.job.source}'")
        e.result.errors.forEach {
            log.error(" @ line: ${it.lineno} - ${it.message} ")
        }
    }

    private fun handle(e:YamlErrorException) {
        log.error("ERROR when parsing YAML file: '${e.source}'")
        log.error(" ${e.message}")
    }

    private fun handle(e: Exception) {
        log.error("ERROR unexpected exception: ${e.message} ", e)
    }

}