package com.ancientlightstudios.simplegen.filters

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class CaseFilterTests : BehaviorSpec({
    Given("A case filter") {
        val filter = CaseFilter()

        When("i filter camel case to hyphenized case") {

            val result = filter.filter("LoremIpsum", null, "upper-camel", "lower-hyphen")
            Then("it is filtered correctly") {
                result shouldBe "lorem-ipsum"
            }
        }
    }
})