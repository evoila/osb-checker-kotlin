package de.evoila.osb.checker.config

import io.restassured.RestAssured
import org.springframework.stereotype.Service
import java.util.*

@Service
class RestAssureConfig(
    configuration: Configuration
) {
  init {
    configuration.correctToken = "Basic ${Base64.getEncoder().encodeToString("${configuration.user}:${configuration.password}".toByteArray())}"
    configuration.wrongUserToken = "Basic ${Base64.getEncoder().encodeToString("${UUID.randomUUID()}:${configuration.password}".toByteArray())}"
    configuration.wrongPasswordToken = "Basic ${Base64.getEncoder().encodeToString("${configuration.user}:${UUID.randomUUID()}".toByteArray())}"

    RestAssured.baseURI = configuration.url
    RestAssured.port = configuration.port
  }

}