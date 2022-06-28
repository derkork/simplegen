package com.ancientlightstudios.simplegen

import io.kotest.matchers.maps.shouldHaveKey

class JavaParserTests : io.kotest.core.spec.style.BehaviorSpec({

    val underTest = JavaParser()

  Given("I have some Java source file") {
      val file = JavaParserTests::class.java.getResourceAsStream("/com/ancientlightstudios/JavaTestClass.java")

      When("i parse the data") {

          val result = underTest.parse(file, "plain text")

          Then("the data is properly parsed") {
              result shouldHaveKey "javaTypes"
          }
      }
  }
})
