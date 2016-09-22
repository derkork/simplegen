package com.ancientlightstudios.simplegen

import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

class TemplateEngineSpecs : Spek({

    given("i have a simple template") {

        val context = mapOf<String, Any>("value1" to "5", "value2" to "10")
        val template = "{{ value1 }}:{{ value2 }}"

        on("executing the template inside the context") {
            val result = TemplateEngine.execute(template, context)

            it("yields the proper output") {
                assertEquals("5:10", result)
            }
        }
    }

    given("i have a template that used environment variables") {
        System.setProperty("someKey", "someValue")
        val template = "{{ 'someKey' | sp }}"

        on("executing the template") {
            val result = TemplateEngine.execute(template, emptyMap())

            it("yields the proper result") {
                assertEquals("someValue", result)
            }
        }
    }

    given("i have a template with a jsonpath filter") {

        val data = YamlReader.readToMap("nestedprop:\n  prop2: value2\n".byteInputStream())
        val context = mapOf<String, Any>("json" to data)
        val template = "{{ json | jsonpath('nestedprop.prop2') }}"

        on("executing the template inside the context") {
            val result = TemplateEngine.execute(template, context)

            it("yields the proper output") {
                assertEquals("value2", result)
            }
        }
    }
})