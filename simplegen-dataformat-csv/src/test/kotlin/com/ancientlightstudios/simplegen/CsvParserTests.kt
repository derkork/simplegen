package com.ancientlightstudios.simplegen

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.maps.shouldHaveKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.intellij.lang.annotations.Language

class CsvParserTests : io.kotest.core.spec.style.BehaviorSpec({

    val underTest = CsvParser()
    underTest.init(mapOf())

    Given("I have some csv data") {

        When("i parse the data") {
            @Language("CSV")
            val data = """
            |name,age
            |john,42
            |jane,43
            |""".trimMargin()
            val result = underTest.parse(data.byteInputStream(), "plain text", mapOf())

            Then("the data is properly parsed") {
                result shouldHaveKey "entries"

                @Suppress("UNCHECKED_CAST")
                val inner = result["entries"] as List<Map<String, Any>>
                inner.size shouldBe 2
                inner[0]["name"] shouldBe "john"
                inner[0]["age"] shouldBe "42"
                inner[1]["name"] shouldBe "jane"
                inner[1]["age"] shouldBe "43"
            }
        }

        When("i parse the data with custom settings") {
            @Language("CSV")
            val data = """
            | four extra lines which will be skipped
            |    
            |    
            |    
            |name,age
            |'john',42
            |jane,43
            |""".trimMargin()

            val settings = mapOf(
                "skipLines" to 4,
                "quoteChar" to "'"
            )
            val result = underTest.parse(data.byteInputStream(), "plain text", settings)

            Then("the data is properly parsed") {
                result shouldHaveKey "entries"

                @Suppress("UNCHECKED_CAST")
                val entries = result["entries"] as List<Map<String, Any>>
                entries.size shouldBe 2
                entries[0]["name"] shouldBe "john"
                entries[0]["age"] shouldBe "42"
                entries[1]["name"] shouldBe "jane"
                entries[1]["age"] shouldBe "43"
            }
        }

        When("i parse invalid CSV") {
            @Language("CSV")
            val invalidData = """
            |name,age
            |john,42,extra data
            |jane,43
            |""".trimMargin()
            val exception = shouldThrow<DataParseException> {
                underTest.parse(invalidData.byteInputStream(), "plain text", mapOf())
            }

            Then("I get a proper exception") {
                exception.origin shouldContain "plain text"
                exception.message shouldContain "The number of data elements is not the same as the number of header elements"
            }

        }
    }
})
