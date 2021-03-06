package com.ancientlightstudios.simplegen.maven

import com.ancientlightstudios.simplegen.Runner
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.io.File

@Suppress("unused")
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresProject = true)
class SimplegenMojo : AbstractMojo() {

    /**
     * The current Maven project.
     */
    @Parameter(property = "project", required = true, readonly = true)
    private lateinit var project: MavenProject

    /**
     * The directory where the simplegen resources are stored
     */
    @Parameter(defaultValue = "\${project.basedir}/src/main/simplegen")
    private lateinit var sourceDirectory: File


    /**
     * The directory where the generated sources should be put
     */
    @Parameter(defaultValue = "\${project.build.directory}/generated-sources/simplegen")
    private lateinit var outputDirectory: File

    /**
     * The name of the config file.
     */
    @Parameter(defaultValue = "config.yml")
    private lateinit var configFileName: String

    /**
     * Should an update of the generated sources be forced.
     */
    @Parameter(defaultValue = "false")
    private var forceUpdate:Boolean = false

    override fun execute() {
        log.info("Generating sources from ${File(sourceDirectory, configFileName).path} to ${outputDirectory.path}")
        if (!Runner(sourceDirectory.path, configFileName, outputDirectory.path, forceUpdate).run()) {
            throw MojoFailureException("There were errors running SimpleGen.")
        }
        project.addCompileSourceRoot(outputDirectory.path)
    }

}