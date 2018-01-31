package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.filters.ScriptFilter
import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TemplateEngineSpecs : Spek({


    given("i have a simple template") {
        val context = mapOf<String, Any>("value1" to "5", "value2" to "10")
        val template = "{{ value1 }}:{{ value2 }}"
        val engine = TemplateEngine(getResourcesRootFileResolver())
        on("executing the template inside the context") {
            val result = engine.execute(TemplateEngineJob("plain text", template).with(context))

            it("yields the proper output") {
                assertEquals("5:10", result)
            }
        }
    }

    given("i have a template that used environment variables") {
        System.setProperty("someKey", "someValue")
        val template = "{{ 'someKey' | sp }}"
        val engine = TemplateEngine(getResourcesRootFileResolver())

        on("executing the template") {
            val result = engine.execute(TemplateEngineJob("plain text", template))

            it("yields the proper result") {
                assertEquals("someValue", result)
            }
        }
    }

    given("i have a template with a jsonpath filter") {

        val data = YamlReader.readToMap("plain text", "nestedprop:\n  prop2: value2\n".byteInputStream())
        val context = mapOf<String, Any>("json" to data)
        val template = "{{ json | jsonpath('nestedprop.prop2') }}"
        val engine = TemplateEngine(getResourcesRootFileResolver())

        on("executing the template inside the context") {
            val result = engine.execute(TemplateEngineJob("plain text", template).with(context))

            it("yields the proper output") {
                assertEquals("value2", result)
            }
        }
    }

    given("i want to use a custom filter") {
        val customFilter = ScriptFilter("plain text", "function reverse(input) {\n    return String(input).split('').reverse().join('');\n}", "reverse")
        val data = mapOf("someString" to "12345")
        val engine = TemplateEngine(getResourcesRootFileResolver(), TemplateEngineArguments(additionalFilters = listOf(customFilter)))

        val template = "{{ someString | reverse }}"

        on("executing the template") {
            val result = engine.execute(TemplateEngineJob("plain text", template).with(data))

            it("yields the proper output") {
                assertEquals("54321", result)
            }
        }
    }

    given("i have a template with a syntax error") {
        val template = "{% if foo %}" // missing endif
        val engine = TemplateEngine(getResourcesRootFileResolver())

        on("executing the template") {
            val exception = catchException {
                engine.execute(TemplateEngineJob("plain text", template))
            }

            it("throws an exception with proper information") {
                assertTrue(exception is TemplateErrorException)
                val templateErrorException = exception as TemplateErrorException
                assertEquals("plain text", templateErrorException.job.source)
                assertEquals(1, templateErrorException.result.errors.size)
                assertEquals(1, templateErrorException.result.errors[0].lineno)
                assertContains(templateErrorException.result.errors[0].message, "Missing end")
            }
        }
    }

    given("i want to use a camelCased filter") {
        val customFilter = ScriptFilter("plain text", "function camelCase(input) {\n    return 'camel';\n}", "camelCase")
        val data = mapOf("someString" to "12345")
        val engine = TemplateEngine(getResourcesRootFileResolver(), TemplateEngineArguments(additionalFilters = listOf(customFilter)))

        val template = "{{ someString | camelCase }}"

        on("executing the template") {
            val result = engine.execute(TemplateEngineJob( "plain text", template).with(data))

            it("yields the proper output") {
                assertEquals("camel", result)
            }
        }
    }
})