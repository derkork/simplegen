package com.ancientlightstudios.simplegen.maven

import com.ancientlightstudios.simplegen.Runner
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File
import java.util.*

@Suppress("unused")
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresProject = true)
class SimplegenGenerateMojo : SimplegenBaseMojo() {

    /**
     * The directory where the simplegen resources are stored
     */
    @Parameter(defaultValue = "\${project.basedir}/src/main/simplegen")
    protected lateinit var sourceDirectory: File

    /**
     * The directory where the generated sources should be put
     */
    @Parameter(defaultValue = "\${project.build.directory}/generated-sources/simplegen")
    protected lateinit var outputDirectory: File


    override fun execute() {
        Workarounds.loadResourceBundle(log)
        log.info("Generating sources from ${File(sourceDirectory, configFileName).path} to ${outputDirectory.path}")
        if (!Runner(sourceDirectory.path, configFileName, outputDirectory.path, forceUpdate).run()) {
            throw MojoFailureException("There were errors running SimpleGen.")
        }

        project.addCompileSourceRoot(outputDirectory.path)
    }

}