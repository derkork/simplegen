package com.ancientlightstudios.simplegen

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class YamlParserTests : BehaviorSpec({

    val underTest = YamlParser()

    Given("I have an erroneous configuration file") {
        //language=yaml
        val stream = "lorem:\n  ipsum: dolor\n- this_is_invalid".byteInputStream()

        When("trying to read the yaml") {
            val exception = shouldThrow<DataParseException> {
                underTest.parse(stream, "plain text")
            }

            Then("throws an exception with additional information") {
                exception.origin shouldBe "plain text"
                exception.message shouldContain "invalid"
            }
        }
    }


})