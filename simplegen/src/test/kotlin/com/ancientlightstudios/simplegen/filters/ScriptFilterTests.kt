package com.ancientlightstudios.simplegen.filters

import com.ancientlightstudios.simplegen.TemplateEngine
import com.ancientlightstudios.simplegen.TemplateEngineArguments
import com.ancientlightstudios.simplegen.TemplateEngineJob
import com.ancientlightstudios.simplegen.getResourcesRootFileResolver
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.intellij.lang.annotations.Language
import javax.script.ScriptException

class ScriptFilterTests : BehaviorSpec({

    Given("I have a script filter with a simple script") {
        @Language("JavaScript")
        val script = "function reverse(input, interpreter, args) {\n   return String(input).split('').reverse().join('');\n}"
        val filter = ScriptFilter("plain text", script, "reverse")

        When("invoking the script filter") {
            val result = filter.filter("1234567890", null)

            Then("executes the script and returns the proper value") {
                result shouldBe "0987654321"
            }
        }
    }


    Given("I have a script filter with a script which calls other functions") {
        @Language("JavaScript")
        val script = "function convert(input) {\n    return String(input);\n}\n\nfunction reverse(input, interpreter, args) {\n   return convert(input).split('').reverse().join('');\n}\n\n"

        val filter = ScriptFilter("plain text", script, "reverse")

        When("invoking the script filter") {
            val result = filter.filter("1234567890", null)

            Then("executes the script and returns the proper value") {
                result shouldBe "0987654321"
            }
        }
    }

    Given("I have a script which doesn't properly handle wrong input") {
        @Language("JavaScript")
        val script = "function problematic(input) { return input.foo(); }"
        val filter = ScriptFilter("plain text", script, "problematic")

        When("invoking the script filter") {
            val exception = shouldThrow<ScriptException> {
                filter.filter(null, null)
            }

            Then ("throws a script exception with the proper error information" ) {
                exception.fileName shouldBe "plain text"
                exception.message shouldContain  "null"
                exception.lineNumber shouldBe 1
            }
        }
    }

    Given( "I have a script which wants to access the template context") {
        val template = "{{ data | getNode }}"

        @Language("JavaScript")
        val script = "function getNode(input, interpreter) { return interpreter('node') }"
        val filter = ScriptFilter("plain text", script, "getNode")

        val engine = TemplateEngine(
                getResourcesRootFileResolver(),
                TemplateEngineArguments(additionalFilters = listOf(filter))
        )

        When ("invoking the script filter through the template engine") {
            val result = engine.execute(
                    TemplateEngineJob("plain text", template)
                            .with("someData", "someNode")
            )

            Then ("has properly gotten the node information from the context") {
                result shouldBe "someNode"
            }
        }
    }

    Given ("I have a script which uses JAVA API") {
        val template = "{{'narf' | pwd_hash('lorem') }}"
        @Language("JavaScript")
        val script="var StandardCharsets = Java.type(\"java.nio.charset.StandardCharsets\");\nvar Mac = Java.type(\"javax.crypto.Mac\");\nvar SecretKeySpec = Java.type(\"javax.crypto.spec.SecretKeySpec\");\nvar Base64 = Java.type(\"java.util.Base64\");\nvar ALGORITHM_NAME = \"HmacSHA512\";\nvar ByteArray = Java.type(\"byte[]\");\n\nfunction pwd_hash(input, interpreter, args) {\n    var inputBytes = input.getBytes(StandardCharsets.UTF_8);\n    var key = args[0].getBytes(StandardCharsets.UTF_8);\n    var mac = Mac.getInstance(ALGORITHM_NAME);\n    var secretKeySpec = new SecretKeySpec(key, ALGORITHM_NAME);\n    mac.init(secretKeySpec);\n    mac.update(inputBytes);\n\n\n    var encodedArray = mac.doFinal();\n\n    var resultArray = new ByteArray(encodedArray.length + 1);\n    resultArray[0] = 2; // version indicator\n    for( var i = 0; i < encodedArray.length; i++) {\n        resultArray[i+1] = encodedArray[i];\n    }\n\n    return Base64.getUrlEncoder().withoutPadding().encodeToString(resultArray);\n}"

        val filter = ScriptFilter("plain text", script, "pwd_hash")

        val engine = TemplateEngine(getResourcesRootFileResolver(), TemplateEngineArguments(additionalFilters = listOf(filter)))

        When ("invoking the script filter through the template engine") {
            val result = engine.execute(TemplateEngineJob("plain text", template))

            Then ("has properly executed the java code") {
                result shouldBe "AkDoonFy-t_Yv6OagRj5xxuQ6JxgkkFOyVMYcwRYL3GQdxCLcboE2BHlaHQVkNVqKE55k2fvxTgLrNolWPeSQjQ"
            }
        }
    }

    Given ("I have a script which wants to log debug") {
        val filter = ScriptFilter("plainText", "function debug(value){console.log(value);}", "debug")
        val engine = TemplateEngine(
                getResourcesRootFileResolver(),
                TemplateEngineArguments(additionalFilters = listOf(filter))
        )

        When("invoking the script filter") {
            val result = engine.execute(TemplateEngineJob("plain text", "{{ 'narf' | debug }}"))
            Then ("throws no exception" ) {
                // i cannot really check the logging side effect here.
                result shouldBe ""
            }
        }
    }
})
