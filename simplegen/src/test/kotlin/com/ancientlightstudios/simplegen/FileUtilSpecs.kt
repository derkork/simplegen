package com.ancientlightstudios.simplegen

import org.jetbrains.spek.api.Spek
import java.io.File
import kotlin.test.assertEquals

class FileUtilSpecs : Spek({

    given("i want to test the file util") {

        on("scanning for template files") {
            val result = FileUtil.resolve(getResourcesRoot(), listOf("**/*.j2"), emptyList())

            it("yields one result") {
                result.size == 1
            }

            it("yields absolute paths") {
                assertEquals(File(getResourcesRoot(), "template.j2").absolutePath, result[0].absolutePath)
            }
        }
    }

})