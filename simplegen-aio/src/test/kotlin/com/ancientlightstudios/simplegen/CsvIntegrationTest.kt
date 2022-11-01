package com.ancientlightstudios.simplegen

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class CsvIntegrationTest : BehaviorSpec({

    val fixture = autoClose(IntegrationTestFixture("/csv-integration-test/config.yml"))

    Given("a runner on a project with CSV files") {
        val runner = fixture.buildRunner()

        When("I run the project") {
            runner.run()
            val outputFile = fixture.resolveOutputFile("result.txt")

            Then("an output file is created") {
                outputFile.exists() shouldBe true
            }
            Then("the output file contains the correct data") {
                outputFile.readText() shouldContain "John is 20 years old."
                outputFile.readText() shouldContain "Mary is 21 years old."
                outputFile.readText() shouldContain "Peter is 22 years old."
            }
        }
    }
})