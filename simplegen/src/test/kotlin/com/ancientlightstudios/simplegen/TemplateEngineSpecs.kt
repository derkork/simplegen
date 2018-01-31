package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.filters.ScriptFilter
import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

class TemplateEngineSpecs : Spek({


    given("i have a simple template") {
        val context = mapOf<String, Any>("value1" to "5", "value2" to "10")
        val template = "{{ value1 }}:{{ value2 }}"
        val engine = TemplateEngine(getResourcesRootFileResolver(), TemplateEngineArguments())
        on("executing the template inside the context") {
            val result = engine.execute(template, context)

            it("yields the proper output") {
                assertEquals("5:10", result)
            }
        }
    }

    given("i have a template that used environment variables") {
        System.setProperty("someKey", "someValue")
        val template = "{{ 'someKey' | sp }}"
        val engine = TemplateEngine(getResourcesRootFileResolver(), TemplateEngineArguments())

        on("executing the template") {
            val result = engine.execute(template, emptyMap())

            it("yields the proper result") {
                assertEquals("someValue", result)
            }
        }
    }

    given("i have a template with a jsonpath filter") {

        val data = YamlReader.readToMap("nestedprop:\n  prop2: value2\n".byteInputStream())
        val context = mapOf<String, Any>("json" to data)
        val template = "{{ json | jsonpath('nestedprop.prop2') }}"
        val engine = TemplateEngine(getResourcesRootFileResolver(), TemplateEngineArguments())

        on("executing the template inside the context") {
            val result = engine.execute(template, context)

            it("yields the proper output") {
                assertEquals("value2", result)
            }
        }
    }

    given("i want to use a custom filter") {
        val customFilter = ScriptFilter("function reverse(input) {\n    return String(input).split('').reverse().join('');\n}", "reverse")
        val data = mapOf("someString" to "12345")
        val engine = TemplateEngine(getResourcesRootFileResolver(), TemplateEngineArguments(additionalFilters = listOf(customFilter)))

        val template="{{ someString | reverse }}"

        on("executing the template")             {
            val result = engine.execute(template, data)

            it("yields the proper output") {
                assertEquals("54321", result)
            }
        }
    }

    given("i want to use a camelCased filter") {
        val customFilter = ScriptFilter("function camelCase(input) {\n    return 'camel';\n}", "camelCase")
        val data = mapOf("someString" to "12345")
        val engine = TemplateEngine(getResourcesRootFileResolver(), TemplateEngineArguments(additionalFilters = listOf(customFilter)))

        val template="{{ someString | camelCase }}"

        on("executing the template")             {
            val result = engine.execute(template, data)

            it("yields the proper output") {
                assertEquals("camel", result)
            }
        }
    }
})