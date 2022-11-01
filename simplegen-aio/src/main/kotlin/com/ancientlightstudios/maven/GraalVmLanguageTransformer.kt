package com.ancientlightstudios.maven

import org.apache.maven.plugins.shade.relocation.Relocator
import org.apache.maven.plugins.shade.resource.ReproducibleResourceTransformer
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * Transformer class for the maven shade plugin that merges GraalVM language
 * specification files.
 */
class GraalVmLanguageTransformer : ReproducibleResourceTransformer {

    private val log = LoggerFactory.getLogger(GraalVmLanguageTransformer::class.java)

    private val targetPath = "META-INF/truffle/language"
    private var matchCount = 0
    private val result = StringBuilder()

    override fun canTransformResource(resource: String): Boolean {
        return resource.contains(targetPath)
    }


    override fun processResource(
        resource: String,
        inputStream: InputStream,
        relocators: MutableList<Relocator>,
        time: Long
    ) {
        matchCount++
        inputStream
            .bufferedReader()
            .lines()
            .forEach { line ->
                result
                    .append(line.replace("language1", "language$matchCount"))
                    .append("\n")
            }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("processResource(resource, inputStream, relocators, time)"))
    override fun processResource(resource: String, inputStream: InputStream, relocators: MutableList<Relocator>) { // NOSONAR
        processResource(resource, inputStream, relocators, 0)
    }

    override fun hasTransformedResource(): Boolean = matchCount > 0

    override fun modifyOutputStream(os: JarOutputStream) {
        val entry = ZipEntry(targetPath)
        os.putNextEntry(entry)
        os.write(result.toString().toByteArray())
        os.closeEntry()
    }
}