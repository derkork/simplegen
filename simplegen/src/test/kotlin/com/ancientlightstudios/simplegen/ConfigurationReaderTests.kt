package com.ancientlightstudios.simplegen

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class ConfigurationReaderTests : BehaviorSpec({

    Given("i have a configuration file") {
        val stream = getTestConfig().inputStream()

        When("loading the configuration file") {
            val config = ConfigurationReader.readConfiguration(stream, getTestConfig().path, getTestConfig().lastModified())

            Then("returns  the configuration with the correct values") {
                config.transformations shouldHaveSize 2
                config.transformations[0].nodes shouldBe "nodes"
                config.transformations[0].template shouldBe "templates/template.j2"
            }
        }
    }



})