package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.configuration.Configuration
import com.ancientlightstudios.simplegen.resources.SimpleFileResolver
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertEquals

class MaterializerSpecs : Spek({

    given("i have a read test configuration") {
        val configuration = YamlReader.readToPojo(getTestConfig().path, getTestConfig().inputStream(), Configuration::class.java)
        val materializer = Materializer(SimpleFileResolver(getResourcesRoot()))

        on("materializing the configuration") {
            val result = materializer.materialize(configuration)

            it("yields 2 materialized configurations") {
                assertEquals(2, result.size)
            }

            it("yields merged data") {
                assertEquals("is foo", (result[0].item.data["root"] as Map<*, *>)["foo"])
            }

        }
    }

})