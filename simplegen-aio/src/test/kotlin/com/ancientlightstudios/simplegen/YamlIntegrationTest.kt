package com.ancientlightstudios.simplegen

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class YamlIntegrationTest : BehaviorSpec({

    val fixture = autoClose(IntegrationTestFixture("/yaml-integration-test/config.yml"))

    Given("a runner on a project with YAML files") {
        val runner = fixture.buildRunner()

        When("I run the project") {
            runner.run()
            val outputFile = fixture.resolveOutputFile("result.txt")

            Then("an output file is created") {
                outputFile.exists() shouldBe true
            }
            Then("the output file contains the correct data") {
                outputFile.readText() shouldContain "works=true"
            }

            listOf("test-lorem", "test-ipsum", "test-dolor" ).forEach { name ->
                val otherOutputFile = fixture.resolveOutputFile("$name.txt")

                Then("an output file is created for $name") {
                    otherOutputFile.exists() shouldBe true
                }

                Then("the output file contains the correct data for $name") {
                    val text = otherOutputFile.readText()
                    text shouldContain "works=true"
                    text shouldContain "node=$name"
                }
            }
        }
    }
})