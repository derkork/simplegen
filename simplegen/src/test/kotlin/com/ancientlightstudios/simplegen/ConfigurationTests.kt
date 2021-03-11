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
                entry.includes[0] shouldBe "data/data.yml"
                entry.excludes should beEmpty()
            }

            Then("yields a result for single includes/excludes") {
                data shouldHaveAtLeastSize 2
                data[1].basePath shouldBe "data/foo"
                data[1].includes[0] shouldBe "**/*.yml"
                data[1].excludes[0] shouldBe "**/narf.yml"
            }

            Then("yields a result for multiple includes/excludes") {
                data shouldHaveAtLeastSize 3

               data[2].basePath shouldBe "data/bar"
               data[2].includes[0] shouldBe "**/*.yml"
               data[2].includes[1] shouldBe "**/*.yaml"
               data[2].excludes[0] shouldBe "**/narf.yml"
               data[2].excludes[1] shouldBe "**/narf.yaml"
            }
        }

        When("getting another data file") {
            val data = configuration.transformations[1].parsedData

            Then("supports specifying data directly at the node without a list") {
                data shouldHaveSize 1
                data[0].basePath shouldBe ""
                data[0].includes[0] shouldBe "data/data.yml"
                data[0].excludes should beEmpty()
            }
        }
    }

})