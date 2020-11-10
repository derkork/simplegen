package com.ancientlightstudios.simplegen

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class XmlIntegrationTest : BehaviorSpec({

    val fixture = autoClose(IntegrationTestFixture("/xml-integration-test/config.yml"))

    Given("a runner on a project with XML files") {
        val runner = fixture.buildRunner()

        When("I run the project") {
            runner.run()
            val outputFile = fixture.resolveOutputFile("result.txt")

            Then("an output file is created") {
                outputFile.exists() shouldBe true
            }
            Then("the output file contains the correct data") {
                outputFile.readText() shouldBe "works=true\njs=eval"
            }
        }
    }
})