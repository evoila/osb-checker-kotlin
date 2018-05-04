package de.evoila.osb.checker.tests

import de.evoila.osb.checker.config.Configuration
import io.restassured.RestAssured

abstract class TestBase {
  init {
    RestAssured.baseURI = Configuration.url
    RestAssured.port = RestAssured.port
    RestAssured.authentication = RestAssured.basic("admin", "cloudfoundry")
  }
}