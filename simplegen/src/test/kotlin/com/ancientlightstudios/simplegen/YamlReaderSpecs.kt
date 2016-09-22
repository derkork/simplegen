package com.ancientlightstudios.simplegen

import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

class YamlReaderSpecs : Spek({

    given("i have a configuration file") {
        val stream = getTestConfig().inputStream()

        on("loading the configuration file") {
            val config = YamlReader.readToPojo(stream, Configuration::class.java)

            it("returns  the configuration with the correct values") {
                assertEquals(2, config.transformations.size)
                assertEquals("nodes", config.transformations[0].nodes)
                assertEquals("template.j2", config.transformations[0].template)
            }
        }
    }

})