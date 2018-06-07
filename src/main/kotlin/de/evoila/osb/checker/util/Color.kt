package de.evoila.osb.checker.util

import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.launcher.TestIdentifier

enum class Color constructor(
    ansiCode: Int) {

  NONE(0),

  BLACK(30),

  RED(31),

  GREEN(32),

  YELLOW(33),

  BLUE(34),

  PURPLE(35),

  CYAN(36),

  WHITE(37);

  private val ansiString: String = "\u001B[" + ansiCode + "m"

  override fun toString(): String {
    return this.ansiString
  }

  companion object {

    fun valueOf(result: TestExecutionResult): Color {
      when (result.status) {
        TestExecutionResult.Status.SUCCESSFUL -> return Color.SUCCESSFUL
        TestExecutionResult.Status.ABORTED -> return Color.ABORTED
        TestExecutionResult.Status.FAILED -> return Color.FAILED
        else -> return Color.NONE
      }
    }

    fun valueOf(testIdentifier: TestIdentifier): Color {
      return if (testIdentifier.isContainer) CONTAINER else TEST
    }

    val SUCCESSFUL = GREEN

    val ABORTED = YELLOW

    val FAILED = RED

    val SKIPPED = PURPLE

    val CONTAINER = CYAN

    val TEST = BLUE

    val DYNAMIC = PURPLE

    val REPORTED = WHITE
  }

}