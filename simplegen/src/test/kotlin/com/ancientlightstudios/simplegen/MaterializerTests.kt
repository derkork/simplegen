package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.resources.SimpleFileResolver
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class MaterializerTests : BehaviorSpec({

    Given("i have a read test configuration") {
        val configuration = ConfigurationReader.readConfiguration(getTestConfig().inputStream(), getTestConfig().path, getTestConfig().lastModified())
        val materializer = Materializer(SimpleFileResolver(getResourcesRoot()))

        When("materializing the configuration") {
            val result = materializer.materialize(configuration)

            Then("yields 2 materialized configurations") {
                result shouldHaveSize 2
            }

            Then("yields merged data") {
                (result[0].item.data["root"] as Map<*, *>)["foo"] shouldBe "is foo"
            }

        }
    }

    Given("i have a test configuration with a relative path in it") {
        val configuration = ConfigurationReader.readConfiguration(getTestConfigRelative().inputStream(), getTestConfigRelative().path, getTestConfigRelative().lastModified())
        val materializer = Materializer(SimpleFileResolver(getResourcesRoot()))

        When("materializing the configuration") {
            val result = materializer.materialize(configuration)

            Then("yields 1 materialized configuration") {
                result shouldHaveSize 1
            }

            Then("yields data from the relative path") {
                result[0].item.data["root"]  shouldBe "this is root"
            }
        }
    }

})