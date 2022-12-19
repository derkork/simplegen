package com.ancientlightstudios.simplegen.maven

import com.ancientlightstudios.simplegen.Runner
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File
import java.util.*

@Suppress("unused")
@Mojo(name = "generateStandalone",  requiresProject = false)
class SimplegenStandaloneMojo : AbstractMojo() {

    /**
     * The name of the config file.
     */
    @Parameter(property = "simplegen.configFileName", defaultValue = "config.yml")
    lateinit var configFileName: String

    /**
     * Should an update of the generated sources be forced.
     */
    @Parameter(property = "simplegen.forceUpdate", defaultValue = "false")
    var forceUpdate: Boolean = false

    /**
     * The directory where the simplegen resources are stored
     */
    @Parameter(property = "simplegen.sourceDirectory", defaultValue = ".")
    lateinit var sourceDirectory: File

    /**
     * The directory where the generated sources should be put
     */
    @Parameter(property = "simplegen.outputDirectory", defaultValue = ".")
    lateinit var outputDirectory: File


    override fun execute() {
        Workarounds.loadResourceBundle(log)
        log.info("Generating sources from ${File(sourceDirectory, configFileName).path} to ${outputDirectory.path}")
        if (!Runner(sourceDirectory.path, configFileName, outputDirectory.path, forceUpdate).run()) {
            throw MojoFailureException("There were errors running SimpleGen.")
        }
    }
}