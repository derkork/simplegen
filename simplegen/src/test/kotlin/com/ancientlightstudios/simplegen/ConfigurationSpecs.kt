package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.configuration.Configuration
import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ConfigurationSpecs : Spek({

    given("i have a read test configuration and a materializer") {
        val configuration = YamlReader.readToPojo(getTestConfig().inputStream(), Configuration::class.java)

        on("reading the configuration") {
            it("reads global engine configuration") {
                assertEquals(true, configuration.templateEngine.trimBlocks)
                assertEquals(true, configuration.templateEngine.lstripBlocks)
                assertEquals(true, configuration.templateEngine.enableRecursiveMacroCalls)
            }

            it("reads per transformation engine configuration") {
                val transformation = configuration.transformations[1]
                assertNotNull(transformation.templateEngine)
                assertEquals(true, transformation.templateEngine?.trimBlocks)
                assertEquals(true, transformation.templateEngine?.trimBlocks)
            }

            it("reads custom filters list") {
                assertEquals(1, configuration.customFilters.size)
                val filter = configuration.customFilters[0]
                assertEquals("filters/reverse_filter.js", filter.script)
                assertEquals("reverse", filter.function)
            }
        }

        on("getting the data files") {
            val data = configuration.transformations[0].getParsedData()

            it("yields a result for the simple case") {
                assert(data.isNotEmpty())
                assertEquals("", data[0].basePath)
                assertEquals("data/data.yml", data[0].includes[0])
                assert(data[0].excludes.isEmpty())
            }

            it("yields a result for single includes/excludes") {
                assert(data.size > 1)
                assertEquals("data/foo", data[1].basePath)
                assertEquals("**/*.yml", data[1].includes[0])
                assertEquals("**/narf.yml", data[1].excludes[0])
            }

            it("yields a result for multiple includes/excludes") {
                assert(data.size > 2)
                assertEquals("data/bar", data[2].basePath)
                assertEquals("**/*.yml", data[2].includes[0])
                assertEquals("**/*.yaml", data[2].includes[1])
                assertEquals("**/narf.yml", data[2].excludes[0])
                assertEquals("**/narf.yaml", data[2].excludes[1])
            }

        }

        on("getting another data file") {
            val data = configuration.transformations[1].getParsedData()

            it("supports specifying data directly at the node without a list") {
                assertEquals(1, data.size)
                assertEquals("", data[0].basePath)
                assertEquals("data/data.yml", data[0].includes[0])
                assert(data[0].excludes.isEmpty())
            }
        }
    }

})