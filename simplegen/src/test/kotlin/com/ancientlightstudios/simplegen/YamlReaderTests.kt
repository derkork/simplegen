package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.configuration.Configuration
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class YamlReaderTests : BehaviorSpec({

    Given("i have a configuration file") {
        val stream = getTestConfig().inputStream()

        When("loading the configuration file") {
            val config = YamlReader.readToPojo(getTestConfig().path, stream, Configuration::class.java)

            Then("returns  the configuration with the correct values") {
                config.transformations shouldHaveSize 2
                config.transformations[0].nodes shouldBe "nodes"
                config.transformations[0].template shouldBe "template.j2"
            }
        }
    }

    Given("I have an erroneous configuration file") {
        //language=yaml
        val stream = "lorem:\n  ipsum: dolor\n- this_is_invalid".byteInputStream()

        When("trying to read the yaml") {
            val exception = shouldThrow<YamlErrorException> {
                YamlReader.readToMap("plain text", stream)
            }

            Then ("throws an exception with additional information") {
                exception.source shouldBe "plain text"
                exception.message shouldContain "invalid"
            }
        }
    }

})