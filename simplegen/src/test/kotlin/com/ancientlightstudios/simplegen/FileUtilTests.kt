package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.resources.FileUtil
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.io.File

class FileUtilTests : BehaviorSpec({

    Given("i want to test the file util") {

        When("scanning for template files") {
            val result = FileUtil.resolve(getResourcesRoot(), listOf("**/*.j2"), emptyList())

            Then("yields one result") {
                result shouldHaveSize 1
            }

            Then("yields absolute paths") {
                result[0].absolutePath shouldBe File(getResourcesRoot(), "templates/template.j2").absolutePath
            }
        }

        When("using relative paths outside the base path") {
            val result = FileUtil.resolve(getResourcesRoot(), "../${getResourcesRootFile().name}")

            Then ("still works") {
                result.canonicalPath shouldBe getResourcesRoot()
            }
        }
    }

})