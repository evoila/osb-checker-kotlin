package de.evoila.osb.checker.tests

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.api.function.ThrowingConsumer
import java.util.stream.Stream
import kotlin.test.assertTrue


class PlayGround {

  @TestFactory
  fun foo(): List<DynamicTest> {

    val strings = listOf("A", "B", "C", "D")

    val dynamicTests = mutableListOf<DynamicTest>()

    strings.forEach {

      dynamicTests.add(
          DynamicTest.dynamicTest("Is $it the Letter A?") {
            run {
              assertTrue("No it is not it was $it") { it == "A" }
            }
          })
    }
    return dynamicTests
  }
}