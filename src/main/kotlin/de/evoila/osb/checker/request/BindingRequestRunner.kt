package de.evoila.osb.checker.request

import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.request.bodies.RequestBody
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.module.jsv.JsonSchemaValidator
import org.springframework.stereotype.Service

@Service
class BindingRequestRunner {

  fun runPutBindingRequest(requestBody: RequestBody, expectedStatusCode: Int, instanceId: String, bindingId: String) {

    val response = RestAssured.with()
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .header(Header("Authorization", Configuration.token))
        .contentType(ContentType.JSON)
        .body(requestBody)
        .put("/v2/service_instances/$instanceId/service_bindings/$bindingId")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(expectedStatusCode)
        .extract()

    val responseBody = response.body().jsonPath().getString("")

    print(responseBody)

    assert(expectedStatusCode == response.statusCode())

    if (response.statusCode() in listOf(200, 201)) {
      JsonSchemaValidator.matchesJsonSchemaInClasspath("binding-response-schema.json").matches(response.body())
    }
  }

  fun runDeleteBindingRequest(serviceId: String?, planId: String?, expectedStatusCode: Int, instanceId: String, bindingId: String) {

    var path = "/v2/service_instances/$instanceId/service_bindings/$bindingId"
    path = serviceId?.let { "$path?service_id=$serviceId" } ?: path

    path = planId?.let { "$path&plan_id=$planId" } ?: path

    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .header(Header("Authorization", Configuration.token))
        .contentType(ContentType.JSON)
        .delete(path)
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(expectedStatusCode)
  }

  fun putWithoutHeader() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", Configuration.token))
        .put("/v2/service_instances/${Configuration.NOT_AN_ID}/service_bindings/${Configuration.NOT_AN_ID}")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(412)
  }

  fun deleteWithoutHeader() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", Configuration.token))
        .put("/v2/service_instances/${Configuration.NOT_AN_ID}/service_bindings/${Configuration.NOT_AN_ID}")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(412)
  }

  fun putNoAuth() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .put("/v2/service_instances/${Configuration.NOT_AN_ID}/service_bindings/${Configuration.NOT_AN_ID}")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
  }

  fun deleteNoAuth() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .delete("/v2/service_instances/${Configuration.NOT_AN_ID}/service_bindings/${Configuration.NOT_AN_ID}")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
  }
}