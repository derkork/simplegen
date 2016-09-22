package com.ancientlightstudios.simplegen


class Runner(private val basePath: String = ".", configPath: String = "config.yml", private val outputFolder: String = ".") {
    private lateinit var config: Configuration
    private lateinit var materializer : Materializer


    init {
        val configFile = FileUtil.resolve(basePath, configPath)
        if (!configFile.exists()) {
            throw IllegalStateException("No configuration file config.yml present.")
        }
        configFile.inputStream().use {
            config = YamlReader.readToPojo(it, Configuration::class.java)
        }

        materializer = Materializer(basePath)
    }


    fun run() {
        val materializedTransformations = materializer.materialize(config)
        for ((template, data, node, outputPath) in materializedTransformations) {
            val outputFile = FileUtil.resolve(outputFolder, outputPath)
            val parent = outputFile.parentFile
            if (!parent.exists()) {
                parent.mkdirs()
            }

            val result = TemplateEngine.execute(template, node, data, basePath)
            outputFile.writeText(result)
        }
    }

}