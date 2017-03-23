package com.ancientlightstudios.simplegen.filters

import com.ancientlightstudios.simplegen.filters.ScriptFilter
import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

class ScriptFilterSpecs : Spek({


    given("I have a script filter with a simple script") {

        val script = "function reverse(input, interpreter, args) {\n   return String(input).split('').reverse().join('');\n}"

        val filter = ScriptFilter(script, "reverse")

        on("invoking the script filter") {
            val result = filter.filter("1234567890", null)

            it("executes the script and returns the proper value") {
                assertEquals("0987654321", result)
            }
        }
    }


    given("I have a script filter with a script which calls other functions") {
        val script = "function convert(input) {\n    return String(input);\n}\n\nfunction reverse(input, interpreter, args) {\n   return convert(input).split('').reverse().join('');\n}\n\n"

        val filter = ScriptFilter(script, "reverse")

        on("invoking the script filter") {
            val result = filter.filter("1234567890", null)

            it("executes the script and returns the proper value") {
                assertEquals("0987654321", result)
            }
        }

    }
})