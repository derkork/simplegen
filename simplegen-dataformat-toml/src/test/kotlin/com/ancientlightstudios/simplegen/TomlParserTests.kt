package com.ancientlightstudios.simplegen

import io.kotest.matchers.maps.shouldHaveKey
import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language

class TomlParserTests : io.kotest.core.spec.style.BehaviorSpec({

    val underTest = TomlParser()

  Given("I have some toml data") {
      @Language("Toml")
      val data = "[foo]\nbar=\"baz\"\nbam=7\n"

      When("i parse the data") {
          val result = underTest.parse(data.byteInputStream(), "plain text")

          Then("the data is properly parsed") {
              result shouldHaveKey "foo"

              @Suppress("UNCHECKED_CAST")
              val inner = result["foo"] as Map<String, Any>

              inner shouldHaveKey "bar"
              inner shouldHaveKey "bam"
              inner["bar"] shouldBe "baz"
              inner["bam"] shouldBe 7
          }
      }
  }
})