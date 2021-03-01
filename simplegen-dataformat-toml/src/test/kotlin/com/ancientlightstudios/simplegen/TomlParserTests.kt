package com.ancientlightstudios.simplegen

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.maps.shouldHaveKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.intellij.lang.annotations.Language
import javax.script.ScriptException

class TomlParserTests : io.kotest.core.spec.style.BehaviorSpec({

    val underTest = TomlParser()

  Given("I have some toml data") {

      When("i parse the data") {
          @Language("Toml")
          val data = "[foo]\nbar=\"baz\"\nbam=7\nzam='\"'\nram=\"'\\\"\"\n"
          val result = underTest.parse(data.byteInputStream(), "plain text")

          Then("the data is properly parsed") {
              result shouldHaveKey "foo"

              @Suppress("UNCHECKED_CAST")
              val inner = result["foo"] as Map<String, Any>

              inner shouldHaveKey "bar"
              inner shouldHaveKey "bam"
              inner shouldHaveKey "zam"
              inner shouldHaveKey "ram"
              inner["bar"] shouldBe "baz"
              inner["bam"] shouldBe 7
              inner["zam"] shouldBe "\""
              inner["ram"] shouldBe "'\""
          }
      }

      When("i parse invalid toml") {
          @Language("Toml")
          val invalidData = "[foo]\nbar=\"baz oops i missed a quote"
          val exception = shouldThrow<DataParseException> {
              underTest.parse(invalidData.byteInputStream(), "plain text")
          }

          Then("I get a proper exception") {
              exception.origin shouldContain "plain text"
              exception.message shouldContain  "Unexpected end of input"

          }

      }
  }
})
