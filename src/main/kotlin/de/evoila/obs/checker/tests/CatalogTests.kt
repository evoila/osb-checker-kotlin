package de.evoila.obs.checker.tests

import de.evoila.obs.checker.Application
import de.evoila.obs.checker.response.Catalog
import de.evoila.obs.checker.util.Performance
import io.restassured.RestAssured.*
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.path.json.JsonPath
import org.slf4j.Logger

object CatalogTests {

  fun runAll(log: Logger, token: String): Catalog {
    withoutHeader(log, token)

    return correctRequest(log, token)//TODO this is a quick hack solution
  }

  private fun withoutHeader(log: Logger, token: String) {
    Performance.logTime(log, "should reject requests without X-Broker-API-Version header with 412") {
      with()
          .header(Header("Authorization", token))
          .get("/v2/catalog")
          .then()
          .assertThat()
          .statusCode(412)
    }
  }

  private fun correctRequest(log: Logger, token: String): Catalog {
    return Performance.logTime(log, "should return list of registered service classes as JSON payload") {
      with()
          .header(Header("X-Broker-API-Version", Application.apiVersion))
          .header(Header("Authorization", token))
          .get("/v2/catalog")
          .then()
          .assertThat()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .extract()
          .response().jsonPath().getObject("", Catalog::class.java)
    }
  }
}