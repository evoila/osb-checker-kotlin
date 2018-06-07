package de.evoila.osb.checker.util

/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

import org.junit.platform.engine.TestExecutionResult

enum class Theme  constructor(
    val tiles: Array<String>
) {

  /**
   * ASCII 7-bit characters form the tree branch.
   *
   *
   * Example test plan execution tree:
   * <pre class="code">
   * +-- engine alpha
   * | '-- container BEGIN
   * |   +-- test 00 [OK]
   * |   '-- test 01 [OK]
   * '-- engine omega
   * +-- container END
   * | +-- test 10 [OK]
   * | '-- test 11 [A] aborted
   * '-- container FINAL
   * +-- skipped [S] because
   * '-- failing [X] BäMM
  </pre> *
   */
  ASCII(arrayOf(".", "| ", "+--", "'--", "[OK]", "[A]", "[X]", "[S]")),
  /**
   * Unicode (extended ASCII) characters are used to display the test execution tree.
   *
   *
   * Example test plan execution tree:
   * <pre class="code">
   * ├─ engine alpha ✔
   * │  └─ container BEGIN ✔
   * │     ├─ test 00 ✔
   * │     └─ test 01 ✔
   * └─ engine omega ✔
   * ├─ container END ✔
   * │  ├─ test 10 ✔
   * │  └─ test 11 ■ aborted
   * └─ container FINAL ✔
   * ├─ skipped ↷ because
   * └─ failing ✘ BäMM
  </pre> *
   */
  UNICODE(arrayOf("╷", "│  ", "├─", "└─", "✔", "■", "✘", "↷"));

  val blank: String = String(CharArray(vertical().length)).replace('\u0000', ' ')

  fun root(): String {
    return tiles[0]
  }

  fun vertical(): String {
    return tiles[1]
  }

  fun blank(): String {
    return blank
  }

  fun entry(): String {
    return tiles[2]
  }

  fun end(): String {
    return tiles[3]
  }

  fun successful(): String {
    return tiles[4]
  }

  fun aborted(): String {
    return tiles[5]
  }

  fun failed(): String {
    return tiles[6]
  }

  fun skipped(): String {
    return tiles[7]
  }

  fun status(result: TestExecutionResult): String {
    when (result.status) {
      TestExecutionResult.Status.SUCCESSFUL -> return successful()
      TestExecutionResult.Status.ABORTED -> return aborted()
      TestExecutionResult.Status.FAILED -> return failed()
      else -> return result.status.name
    }
  }

  /**
   * Return lower case [.name] for easier usage in help text for
   * available options.
   */
  override fun toString(): String {
    return name.toLowerCase()
  }

  companion object {

    fun valueOf(charset: Charset): Theme {
      return if (StandardCharsets.UTF_8 == charset) {
        UNICODE
      } else ASCII
    }
  }
}