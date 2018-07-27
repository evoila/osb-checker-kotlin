package de.evoila.osb.checker.config

import io.restassured.RestAssured
import org.springframework.stereotype.Service
import java.util.*

@Service
class RestAssureConfig(
    configuration: Configuration
) {
  init {
    configuration.token = "Basic ${Base64.getEncoder().encodeToString("${configuration.user}:${configuration.password}".toByteArray())}"

    RestAssured.baseURI = configuration.url
    RestAssured.port = configuration.port
  }

}