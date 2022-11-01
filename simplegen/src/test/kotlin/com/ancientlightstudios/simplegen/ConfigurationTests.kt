package com.ancientlightstudios.simplegen

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.shouldNotBe

class ConfigurationTests : BehaviorSpec({

    Given("i have a read test configuration and a materializer") {
        val configuration = ConfigurationReader.readConfiguration(
            getTestConfig().inputStream(),
            getTestConfig().path,
            getTestConfig().lastModified()
        )

        When("reading the configuration") {
            Then("it reads global engine configuration") {
                configuration.templateEngine.trimBlocks shouldBe true
                configuration.templateEngine.lstripBlocks shouldBe true
                configuration.templateEngine.enableRecursiveMacroCalls shouldBe true
            }

            Then("it reads per transformation engine configuration") {
                val transformation = configuration.transformations[1]
                transformation shouldNotBe null
                transformation.templateEngine?.trimBlocks shouldBe true
                transformation.templateEngine?.lstripBlocks shouldBe true
                transformation.templateEngine?.enableRecursiveMacroCalls shouldBe true
            }

            Then("it reads custom filters list") {
                configuration.customFilters.size shouldBe 1
                val filter = configuration.customFilters[0]
                filter.script shouldBe "filters/reverse_filter.js"
                filter.function shouldBe "reverse"
            }
        }

        When("getting the data files") {
            val data = configuration.transformations[0].parsedData

            Then("it yields a result for the simple case") {
                data shouldNot beEmpty()

                val entry = data[0]
                entry.basePath shouldBe ""
                // special case: only a single file name was given
                entry.file shouldBe "data/data.yml"
                entry.includes should beEmpty()
                entry.excludes should beEmpty()
            }

            Then("yields a result for single includes/excludes") {
                data shouldHaveAtLeastSize 2
                val entry = data[1]
                entry.file shouldBe null
                entry.basePath shouldBe "data/foo"
                entry.includes[0] shouldBe "**/*.yml"
                entry.excludes[0] shouldBe "**/narf.yml"
            }

            Then("yields a result for multiple includes/excludes") {
                data shouldHaveAtLeastSize 3

                val entry = data[2]
                entry.file shouldBe null
                entry.basePath shouldBe "data/bar"
               entry.includes[0] shouldBe "**/*.yml"
               entry.includes[1] shouldBe "**/*.yaml"
               entry.excludes[0] shouldBe "**/narf.yml"
               entry.excludes[1] shouldBe "**/narf.yaml"
            }
        }

        When("getting another data file") {
            val data = configuration.transformations[1].parsedData

            Then("supports specifying data directly at the node without a list") {
                data shouldHaveSize 1
                val entry = data[0]
                entry.file shouldBe "data/data.yml"
                entry.basePath shouldBe ""
                entry.includes should beEmpty()
                entry.excludes should beEmpty()
            }
        }
    }

})