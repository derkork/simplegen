package com.ancientlightstudios.simplegen

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class HtmlIntegrationTest : BehaviorSpec({

    val fixture = autoClose(IntegrationTestFixture("/html-integration-test/config.yml"))

    Given("a runner on a project with HTML files") {
        val runner = fixture.buildRunner()

        When("I run the project") {
            runner.run()
            val outputFile = fixture.resolveOutputFile("result.txt")

            Then("an output file is created") {
                outputFile.exists() shouldBe true
            }
            Then("the output file contains the correct data") {
                val generatedText = outputFile.readText()
                generatedText shouldContain   "title=Example"
                generatedText shouldContain   "text=This is an example of a simple HTML page with one paragraph."
                generatedText shouldContain   "ownText=Lorem ipsum amet."
                generatedText shouldContain   "nestedText=Lorem ipsum dolor sit amet."
            }
        }
    }
})
