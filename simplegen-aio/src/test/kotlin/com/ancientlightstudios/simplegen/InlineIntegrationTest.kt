package com.ancientlightstudios.simplegen

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class InlineIntegrationTest : BehaviorSpec({

    val fixture = autoClose(IntegrationTestFixture("/inline-integration-test/config.yml"))

    Given("a runner on a project with inline data definitions") {
        val runner = fixture.buildRunner()

        When("I run the project") {
            runner.run()
            val outputFile = fixture.resolveOutputFile("result.txt")

            Then("an output file is created") {
                outputFile.exists() shouldBe true
            }
            Then("the output file contains the correct data") {
                outputFile.readText() shouldBe "works=true"
            }
        }
    }
})