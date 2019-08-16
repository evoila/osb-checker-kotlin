package de.evoila.osb.checker.request

import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.response.catalog.Catalog
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import org.springframework.stereotype.Service

@Service
class CatalogRequestRunner(
    configuration: Configuration
) : RequestHandler(configuration) {

  fun withoutHeader() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", configuration.correctToken))
        .get("/v2/catalog")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(412)
  }

  fun correctRequestAndValidateResponse() {

    val catalogSchema = matchesJsonSchemaInClasspath("catalog-schema.json")

    RestAssured.with()
        .log().ifValidationFails()
        .headers(validHeaders)
        .get("/v2/catalog")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body(catalogSchema)
  }

  fun correctRequest(): Catalog {
    return RestAssured.with()
        .log().ifValidationFails()
        .headers(validHeaders)
        .get("/v2/catalog")
        .then()
        .log().ifValidationFails()
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
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .get("/v2/catalog")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
  }

  fun wrongUser() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .header(Header("Authorization", configuration.wrongUserToken))
        .get("/v2/catalog")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
  }

  fun wrongPassword() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .header(Header("Authorization", configuration.wrongPasswordToken))
        .get("/v2/catalog")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
  }
}