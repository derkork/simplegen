package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.configuration.Configuration
import com.ancientlightstudios.simplegen.resources.FileNotResolvedException
import com.ancientlightstudios.simplegen.resources.FileResolver
import com.ancientlightstudios.simplegen.resources.FileUtil
import com.ancientlightstudios.simplegen.resources.SimpleFileResolver
import java.io.File


class Runner(basePath: String = ".", configPath: String = "config.yml", private val outputFolder: String = ".") {
    private lateinit var config: Configuration
    private val fileResolver: FileResolver
    private var materializer: Materializer


    init {
        fileResolver = SimpleFileResolver(basePath)

        val configFile: File
        try {
            configFile = FileUtil.resolve(basePath, configPath)
        } catch(e: FileNotResolvedException) {
            throw IllegalStateException("No configuration file config.yml present.")
        }

        configFile.inputStream().use {
            config = YamlReader.readToPojo(it, Configuration::class.java)
        }

        materializer = Materializer(fileResolver)
    }


    fun run() {
        val materializedTransformations = materializer.materialize(config)
        val filters = config.customFilters.map { FilterBuilder.buildFilter(it, fileResolver) }

        for ((template, data, node, outputPath, engineConfiguration) in materializedTransformations) {
            val outputFile = FileUtil.resolve(outputFolder, outputPath)
            val parent = outputFile.parentFile
            if (!parent.exists()) {
                parent.mkdirs()
            }

            val engineArguments = TemplateEngineArguments(engineConfiguration, filters)
            val templateEngine = TemplateEngine(fileResolver, engineArguments)

            val result = templateEngine.execute(template, node, data)
            outputFile.writeText(result)
        }
    }

}