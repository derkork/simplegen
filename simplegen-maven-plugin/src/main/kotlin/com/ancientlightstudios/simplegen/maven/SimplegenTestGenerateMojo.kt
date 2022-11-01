package com.ancientlightstudios.simplegen.maven

import com.ancientlightstudios.simplegen.Runner
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File

@Suppress("unused")
@Mojo(name = "testGenerate", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES, requiresProject = true)
class SimplegenTestGenerateMojo : SimplegenBaseMojo() {

    /**
     * The directory where the simplegen resources are stored
     */
    @Parameter(defaultValue = "\${project.basedir}/src/test/simplegen")
    private lateinit var sourceDirectory: File

    /**
     * The directory where the generated sources should be put
     */
    @Parameter(defaultValue = "\${project.build.directory}/generated-test-sources/simplegen")
    private lateinit var outputDirectory: File


    override fun execute() {
        log.info("Generating test sources from ${File(sourceDirectory, configFileName).path} to ${outputDirectory.path}")
        if (!Runner(sourceDirectory.path, configFileName, outputDirectory.path, forceUpdate).run()) {
            throw MojoFailureException("There were errors running SimpleGen.")
        }

        project.addTestCompileSourceRoot(outputDirectory.path)
    }

}