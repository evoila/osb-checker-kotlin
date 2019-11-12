package de.evoila.osb.checker.request

import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.response.catalog.Catalog
import io.restassured.RestAssured
import io.restassured.http.Header
import io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import org.springframework.stereotype.Service
import java.util.*
import io.restassured.response.ExtractableResponse
import io.restassured.response.Response

@Service
class CatalogRequestRunner(
        configuration: Configuration
) : RequestHandler(configuration) {

    fun withoutHeader() {
        RestAssured.with()
                .log().ifValidationFails()
                .auth().basic(configuration.user, configuration.password)
                .get("/v2/catalog")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(412)
    }

    fun withSpecificHeaderAndNoAssertion(key: String, value: String): ExtractableResponse<Response> {
        validRequestHeaders[key] = value

        return RestAssured.with()
                .log().ifValidationFails()
                .headers(validRequestHeaders)
                .get("/v2/catalog")
                .then()
                .log().ifValidationFails()
                .extract()
    }


    fun correctRequestAndValidateResponse() {
        if (configuration.apiVersion >= 2.15 && configuration.useRequestIdentity) {
            useRequestIdentity("OSB-Checker-validate-GET-catalog-${UUID.randomUUID()}")
        }

        RestAssured.with()
                .log().ifValidationFails()
                .headers(validRequestHeaders)
                .auth().basic(configuration.user, configuration.password)
                .get("/v2/catalog")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("catalog-schema.json"))
                .headers(expectedResponseHeaders)
    }

    fun correctRequest(): Catalog {
        if (configuration.apiVersion >= 2.15 && configuration.useRequestIdentity) {
            useRequestIdentity("OSB-Checker-GET-catalog-${UUID.randomUUID()}")
        }

        return RestAssured.with()
                .log().ifValidationFails()
                .headers(validRequestHeaders)
                .get("/v2/catalog")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(200)
                .headers(expectedResponseHeaders)
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
                .header(Header("Authorization", wrongUsernameToken))
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
                .header(Header("Authorization", wrongPasswordToken))
                .get("/v2/catalog")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(401)
    }
}