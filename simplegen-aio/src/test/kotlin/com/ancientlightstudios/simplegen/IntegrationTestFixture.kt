package com.ancientlightstudios.simplegen

import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * Helper class for quickly setting up test fixtures for integration tests. It is auto-closable and will
 * delete the temporary output folder on close.
 */
class IntegrationTestFixture(configFile: String) : AutoCloseable {
    private val log = LoggerFactory.getLogger(IntegrationTestFixture::class.java)

    private val folder =
        File(IntegrationTestFixture::class.java.getResource(configFile).toURI()).parentFile
    private val outputFolder =
        Files.createTempDirectory("IntegrationTestFixture")

    fun buildRunner() =
        Runner(folder.canonicalPath, outputFolder = outputFolder.toFile().canonicalPath, forceUpdate = true)

    fun resolveOutputFile(name: String): File = outputFolder.resolve(name).toFile()

    override fun close() {
        log.info("Cleaning up temporary files in '$outputFolder'")
        // recursively delete the temp folder
        Files.walk(outputFolder)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete)
    }
}