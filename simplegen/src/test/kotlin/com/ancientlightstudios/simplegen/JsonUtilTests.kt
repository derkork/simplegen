package com.ancientlightstudios.simplegen

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.maps.shouldContainKeys

class JsonUtilTests : BehaviorSpec({

    Given("i have two simple maps") {

        val node1 = mapOf(
                "narf" to mapOf<String, Any>(
                        "narf" to "barf"
                ),
                "narf3" to "arf"
        )
        val node2 = mapOf<String, Any>(
                "narf" to mapOf<String, Any>(
                        "narf2" to "barf"
                ),
                "narf2" to mapOf<String, Any>(
                        "narf" to "barf",
                        "narf2" to "barf"
                )
        )

        When("merging these maps") {
            val result = JsonUtil.merge(node1, node2)

            Then("it merges the top level objects correctly") {
                result.shouldContainKeys("narf", "narf2", "narf3")
            }

            Then("it merges second level objects correctly") {
                @Suppress("UNCHECKED_CAST")
                (result["narf"] as Map<String, Any>).shouldContainKeys("narf2")
                @Suppress("UNCHECKED_CAST")
                (result["narf2"] as Map<String, Any>).shouldContainKeys("narf")
            }
        }
    }


    Given("i have some maps with nested lists") {
        val node1 = mapOf<String, Any>(
                "narf" to listOf("lorem", "ipsum")
        )
        val node2 = mapOf<String, Any>(
                "narf" to listOf("dolor", "sit")
        )

        When("merging those lists") {
            val result = JsonUtil.merge(node1, node2)
            val finalList = result["narf"] as List<*>

            Then("merges the lists") {
                finalList.shouldContainAll("lorem", "ipsum", "dolor", "sit")
            }
        }
    }

})