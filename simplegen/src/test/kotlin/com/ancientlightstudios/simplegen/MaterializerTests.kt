package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.configuration.Configuration
import com.ancientlightstudios.simplegen.resources.SimpleFileResolver
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class MaterializerTests : BehaviorSpec({

    Given("i have a read test configuration") {
        val configuration = YamlReader.readToPojo(getTestConfig().path, getTestConfig().inputStream(), Configuration::class.java)
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

})