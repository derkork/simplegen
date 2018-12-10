package com.ancientlightstudios.simplegen.filters

import com.ancientlightstudios.simplegen.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
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

    given( "I have a script which wants to access the template context") {
        val template = "{{ data | getNode }}"

        val script = "function getNode(input, interpreter) { return interpreter.context['node'] }"
        val filter = ScriptFilter("plain text", script, "getNode")

        val engine = TemplateEngine(getResourcesRootFileResolver(), TemplateEngineArguments(additionalFilters = listOf(filter)))
        val result = engine.execute(TemplateEngineJob("plain text", template).with("someData", "someNode"))

        on ("invoking the script filter through the template engine") {
            it ("has properly gotten the node information from the context") {
                assertEquals("someNode", result)
            }
        }
    }
})