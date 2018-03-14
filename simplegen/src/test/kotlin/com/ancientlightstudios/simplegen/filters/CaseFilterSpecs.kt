package com.ancientlightstudios.simplegen.filters

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertEquals

class CaseFilterSpecs : Spek({


    given("I have a case filter") {
        val filter = CaseFilter()

        on("filtering camel case to hypenized case") {
            val result = filter.filter("LoremIpsum", null, "upper-camel", "lower-hyphen")

            it("is filtered correctly") {
                assertEquals("lorem-ipsum", result)
            }
        }
    }
})