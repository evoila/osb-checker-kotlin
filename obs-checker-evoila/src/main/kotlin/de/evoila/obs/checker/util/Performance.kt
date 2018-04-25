package de.evoila.obs.checker.util

import org.slf4j.Logger
import java.time.Duration
import java.time.Instant

object Performance {

  fun <T> time(block: () -> T): Pair<T, Duration> {
    val start = Instant.now()

    val result = block()

    val end = Instant.now()
    val duration = Duration.between(start, end)

    return result to duration
  }

  fun <T> logTime(log: Logger, operation: String, block: () -> T): T {
    log.info("Starting $operation")
    val (result, duration) = time(block)
    log.info("Finished $operation, took ${duration.toMillis()} ms")

    return result
  }

}