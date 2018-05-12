package de.evoila.osb.checker.tests

import de.evoila.osb.checker.config.Configuration
import io.restassured.RestAssured
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TestBase(
    val configuration: Configuration
) {

  init {
    RestAssured.baseURI = configuration.url
    RestAssured.port = configuration.port
    RestAssured.authentication = RestAssured.basic("admin", "cloudfoundry")
  }
}