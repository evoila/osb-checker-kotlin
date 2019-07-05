package de.evoila.osb.checker.config

import io.restassured.RestAssured
import org.springframework.stereotype.Service
import java.util.*

@Service
class RestAssureConfig(
    configuration: Configuration
) {
  init {
    configuration.correctToken = encode(configuration.user, configuration.password)
    configuration.wrongUserToken = encode(UUID.randomUUID().toString(), configuration.password)
    configuration.wrongPasswordToken = encode(configuration.user, UUID.randomUUID().toString())

    RestAssured.baseURI = configuration.url
    RestAssured.port = configuration.port
    if (configuration.skipTLSVerification) {
      RestAssured.useRelaxedHTTPSValidation()
    }
  }

  private fun encode(user: String, password: String): String =
      "Basic ${Base64.getEncoder().encodeToString("$user:$password".toByteArray())}"
}