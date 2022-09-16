package com.ancientlightstudios.simplegen.filters

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class FlattenFilterTest : BehaviorSpec({
    Given("A flatten filter") {
        val filter = FlattenFilter()

        When("i filter a string") {

            val result = filter.filter("LoremIpsum",null)
            Then("i get it back") {
                result shouldBe "LoremIpsum"
            }
        }

        When("i filter a null value") {
            val result = filter.filter(null,null)
            Then("i get it back") {
                result shouldBe null
            }
        }

        When( "i filter an array with nested values") {
            val result = filter.filter(arrayOf(1,2, arrayOf("foo","bar")), null)
            Then("i get it back flattened") {
                result shouldBe arrayOf(1,2,"foo","bar")
            }
        }

        When("i filter an array without nested values") {
            val result = filter.filter(arrayOf(1,2, "foo", "bar"), null)
            Then("i get it back") {
                result shouldBe arrayOf(1,2, "foo", "bar")
            }
        }

        When("i filter a collection with nested values") {
            val result = filter.filter(listOf(1,2, listOf("foo","bar")), null)
            Then("i get it back flattened") {
                result shouldBe listOf(1,2,"foo","bar")
            }
        }

        When("i filter a collection without nested values") {
            val result = filter.filter(listOf(1,2, "foo", "bar"), null)
            Then("i get it back") {
                result shouldBe listOf(1,2, "foo", "bar")
            }
        }

    }
})