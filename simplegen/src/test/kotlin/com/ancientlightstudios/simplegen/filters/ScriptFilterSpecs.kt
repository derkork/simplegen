package com.ancientlightstudios.simplegen.filters

import com.ancientlightstudios.simplegen.assertContains
import com.ancientlightstudios.simplegen.catchException
import org.jetbrains.spek.api.Spek
import javax.script.ScriptException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScriptFilterSpecs : Spek({


    given("I have a script filter with a simple script") {

        val script = "function reverse(input, interpreter, args) {\n   return String(input).split('').reverse().join('');\n}"

        val filter = ScriptFilter("plain text", script, "reverse")

        on("invoking the script filter") {
            val result = filter.filter("1234567890", null)

            it("executes the script and returns the proper value") {
                assertEquals("0987654321", result)
            }
        }
    }


    given("I have a script filter with a script which calls other functions") {
        val script = "function convert(input) {\n    return String(input);\n}\n\nfunction reverse(input, interpreter, args) {\n   return convert(input).split('').reverse().join('');\n}\n\n"

        val filter = ScriptFilter("plain text", script, "reverse")

        on("invoking the script filter") {
            val result = filter.filter("1234567890", null)

            it("executes the script and returns the proper value") {
                assertEquals("0987654321", result)
            }
        }

    }

    given("I have a script which doesn't properly handle wrong input") {
        val script = "function problematic(input) { return input.foo(); }"
        val filter = ScriptFilter("plain text", script, "problematic")

        on("invoking the script filter") {
            val exception = catchException {
                filter.filter(null, null)
            }

            it ("throws a script exception with the proper error information" ) {
                assertTrue( exception is ScriptException)
                val scriptException = exception as ScriptException
                assertEquals("plain text", scriptException.fileName)
                assertContains(scriptException.message, "null")
                assertEquals(scriptException.lineNumber, 1)
            }
        }
    }
})