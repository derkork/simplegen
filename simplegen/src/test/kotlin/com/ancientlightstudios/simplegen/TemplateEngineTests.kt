package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.filters.ScriptFilter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class TemplateEngineTests : BehaviorSpec({


    Given("i have a simple template") {
        val context = mapOf<String, Any>("value1" to "5", "value2" to "10")
        val template = "{{ value1 }}:{{ value2 }}"
        val engine = TemplateEngine(getResourcesRootFileResolver())
        When("executing the template inside the context") {
            val result = engine.execute(TemplateEngineJob("plain text", template).with(context))

            Then("yields the proper output") {
                result shouldBe "5:10"
            }
        }
    }

    Given("i have a template that used environment variables") {
        System.setProperty("someKey", "someValue")
        val template = "{{ 'someKey' | sp }}"
        val engine = TemplateEngine(getResourcesRootFileResolver())

        When("executing the template") {
            val result = engine.execute(TemplateEngineJob("plain text", template))

            Then("yields the proper result") {
                result shouldBe "someValue"
            }
        }
    }

    Given("i have a template with a jsonpath filter") {

        val data = YamlParser().parse("nestedprop:\n  prop2: value2\n".byteInputStream(), "plain text", mapOf())
        val context = mapOf<String, Any>("json" to data)
        val template = "{{ json | jsonpath('nestedprop.prop2') }}"
        val engine = TemplateEngine(getResourcesRootFileResolver())

        When("executing the template inside the context") {
            val result = engine.execute(TemplateEngineJob("plain text", template).with(context))

            Then("yields the proper output") {
                result shouldBe "value2"
            }
        }
    }

    Given("i want to use a custom filter") {
        val customFilter = ScriptFilter("plain text", "function reverse(input) {\n    return String(input).split('').reverse().join('');\n}", "reverse")
        val data = mapOf("someString" to "12345")
        val engine = TemplateEngine(getResourcesRootFileResolver(), TemplateEngineArguments(additionalFilters = listOf(customFilter)))

        val template = "{{ someString | reverse }}"

        When("executing the template") {
            val result = engine.execute(TemplateEngineJob("plain text", template).with(data))

            Then("yields the proper output") {
                result shouldBe "54321"
            }
        }
    }

    Given("i have a template with a syntax error") {
        val template = "{% if foo %}" // missing endif
        val engine = TemplateEngine(getResourcesRootFileResolver())

        When("executing the template") {
            val exception = shouldThrow<TemplateErrorException> {
                engine.execute(TemplateEngineJob("plain text", template))
            }

            Then("throws an exception with proper information") {
                exception.job.source shouldBe "plain text"
                exception.result.errors shouldHaveSize 1
                exception.result.errors[0].lineno shouldBe 1
                exception.result.errors[0].message shouldContain "Missing end"
            }
        }
    }

    Given("i want to use a camelCased filter") {
        val customFilter = ScriptFilter("plain text", "function camelCase(input) {\n    return 'camel';\n}", "camelCase")
        val data = mapOf("someString" to "12345")
        val engine = TemplateEngine(getResourcesRootFileResolver(), TemplateEngineArguments(additionalFilters = listOf(customFilter)))

        val template = "{{ someString | camelCase }}"

        When("executing the template") {
            val result = engine.execute(TemplateEngineJob( "plain text", template).with(data))

            Then("yields the proper output") {
                result shouldBe "camel"
            }
        }
    }
})