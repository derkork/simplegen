package com.ancientlightstudios.simplegen

import org.jetbrains.spek.api.Spek

class JsonUtilSpecs : Spek({

    given("i have two simple maps") {

        val node1 = mapOf<String, Any>(
                "narf" to mapOf<String, Any>(
                        "narf" to "barf"
                )
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

        on("merging these maps") {
            val result = JsonUtil.merge(node1, node2)

            it("it merges the top level objects correctly") {
                assert(result.containsKey("narf"))
                assert(result.containsKey("narf2"))
            }

            it("it merges second level objects correctly") {
                @Suppress("UNCHECKED_CAST")
                assert((result["narf"] as Map<String,Any>).containsKey("narf2"))
                @Suppress("UNCHECKED_CAST")
                assert((result["narf2"] as Map<String,Any>).containsKey("narf"))
            }
        }
    }

})