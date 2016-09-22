package com.ancientlightstudios.simplegen

import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

class MaterializerSpecs : Spek({

    given("i have a read test configuration") {
        val configuration = YamlReader.readToPojo(getTestConfig().inputStream(), Configuration::class.java)
        val materializer = Materializer(getResourcesRoot())

        on("materializing the configuration") {
            val result = materializer.materialize(configuration)

            it("yields 2 materialized configurations") {
                assertEquals(2, result.size)
            }

            it("yields merged data") {
                assertEquals("is foo", (result[0].data["root"] as Map<*, *>)["foo"])
            }

        }
    }

})