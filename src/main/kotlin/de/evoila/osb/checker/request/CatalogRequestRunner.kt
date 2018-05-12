package de.evoila.osb.checker.request

import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.response.Catalog
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import org.springframework.beans.factory.annotation.Autowired

class CatalogRequestRunner(
    private val token: String
) {

  @Autowired
  lateinit var configuration: Configuration

  fun withoutHeader() {
    RestAssured.with()
        .header(Header("Authorization", token))
        .get("/v2/catalog")
        .then()
        .assertThat()
        .statusCode(412)
  }

  fun correctRequestAndValidateResponse() {

    val catalogSchema = matchesJsonSchemaInClasspath("catalog-schema.json")

    RestAssured.with()
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .header(Header("Authorization", token))
        .get("/v2/catalog")
        .then()
        .assertThat()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body(catalogSchema)
  }

  fun correctRequest(): Catalog {
    return RestAssured.with()
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .header(Header("Authorization", token))
        .get("/v2/catalog")
        .then()
        .assertThat()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .extract()
        .response()
        .jsonPath()
        .getObject("", Catalog::class.java)
  }

  fun noAuth() {
    RestAssured.with()
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .header(Header("Authorization", token))
        .get("/v2/catalog")
        .then()
        .assertThat()
        .statusCode(401)
  }
}