package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.configuration.Configuration
import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class YamlReaderSpecs : Spek({

    given("i have a configuration file") {
        val stream = getTestConfig().inputStream()

        on("loading the configuration file") {
            val config = YamlReader.readToPojo(getTestConfig().path, stream, Configuration::class.java)

            it("returns  the configuration with the correct values") {
                assertEquals(2, config.transformations.size)
                assertEquals("nodes", config.transformations[0].nodes)
                assertEquals("template.j2", config.transformations[0].template)
            }
        }
    }

    given("I have an erroneous configuration file") {
        //language=yaml
        val stream = "lorem:\n  ipsum: dolor\n- this_is_invalid".byteInputStream()

        on("trying to read the yaml") {
            val exception = catchException {
                YamlReader.readToMap("plain text", stream)
            }

            it ("throws an exception with additional information") {
                assertTrue(exception is YamlErrorException)
                val yamlErrorException = exception as YamlErrorException
                assertEquals("plain text", yamlErrorException.source)
                assertContains(yamlErrorException.message, "invalid")
            }
        }
    }

})