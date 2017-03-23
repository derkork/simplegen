package com.ancientlightstudios.simplegen

import com.ancientlightstudios.simplegen.resources.FileUtil
import org.jetbrains.spek.api.Spek
import java.io.File
import kotlin.test.assertEquals

class FileUtilSpecs : Spek({

    given("i want to test the file util") {

        on("scanning for template files") {
            val result = FileUtil.resolve(getResourcesRoot(), listOf("**/*.j2"), emptyList())

            it("yields one result") {
                assertEquals(1, result.size)
            }

            it("yields absolute paths") {
                assertEquals(File(getResourcesRoot(), "template.j2").absolutePath, result[0].absolutePath)
            }
        }

        on("using relative paths outside the base path") {
            val result = FileUtil.resolve(getResourcesRoot(), "../${getResourcesRootFile().name}")

            it ("still works") {
                assertEquals(getResourcesRoot(), result.canonicalPath)
            }
        }
    }

})