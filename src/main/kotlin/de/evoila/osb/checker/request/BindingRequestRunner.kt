package de.evoila.osb.checker.request

import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.request.bodies.RequestBody
import de.evoila.osb.checker.response.LastOperationResponse.State
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.module.jsv.JsonSchemaValidator
import io.restassured.response.ExtractableResponse
import io.restassured.response.Response
import org.hamcrest.collection.IsIn
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class BindingRequestRunner(
    configuration: Configuration
) : PollingRequestHandler(
    configuration
) {

  fun runGetBindingRequest(expectedStatusCode: Int, instanceId: String, bindingId: String) {
    val response = RestAssured.with()
        .log().ifValidationFails()
        .headers(validHeaders)
        .contentType(ContentType.JSON)
        .get("/v2/service_instances/$instanceId/service_bindings/$bindingId")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(expectedStatusCode)
        .extract()

    JsonSchemaValidator.matchesJsonSchemaInClasspath("fetch-binding-response-schema.json")
        .matches(response.body())
  }

  fun runPutBindingRequest(requestBody: RequestBody,
                           instanceId: String,
                           bindingId: String,
                           vararg expectedStatusCodes: Int): ExtractableResponse<Response> {
    val response = RestAssured.with()
        .log().ifValidationFails()
        .headers(validHeaders)
        .contentType(ContentType.JSON)
        .body(requestBody)
        .param("accepts_incomplete", configuration.apiVersion > 2.13)
        .put("/v2/service_instances/$instanceId/service_bindings/$bindingId")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(IsIn(expectedStatusCodes.asList()))
        .extract()

    if (response.statusCode() == 200) {
      JsonSchemaValidator.matchesJsonSchemaInClasspath("binding-response-schema.json")
          .matches(response.body())
    }

    return response
  }

  fun polling(instanceId: String,
              bindingId: String,
              expectedFinalStatusCode: Int,
              operationData: String,
              maxPollingDuration: Int): State {
    val latestAcceptablePollingInstant = Instant.now().plusSeconds(maxPollingDuration.toLong())

    return waitForFinish(
        path = "/v2/service_instances/$instanceId/service_bindings/$bindingId/last_operation",
        expectedFinalStatusCode = expectedFinalStatusCode,
        operationData = operationData,
        latestAcceptablePollingInstant = latestAcceptablePollingInstant)
  }

  fun runDeleteBindingRequest(serviceId: String?,
                              planId: String?,
                              instanceId: String,
                              bindingId: String, vararg expectedStatusCodes: Int): ExtractableResponse<Response> {
    var path = "/v2/service_instances/$instanceId/service_bindings/$bindingId"
    path = serviceId?.let { "$path?service_id=$serviceId" } ?: path

    return RestAssured.with()
        .log().ifValidationFails()
        .headers(validHeaders)
        .contentType(ContentType.JSON)
        .param("service_id", serviceId)
        .param("plan_id", planId)
        .param("accepts_incomplete", configuration.apiVersion > 2.13)
        .delete(path)
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(IsIn(expectedStatusCodes.asList()))
        .extract()
  }

  fun putWithoutHeader() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", configuration.correctToken))
        .put("/v2/service_instances/${Configuration.notAnId}/service_bindings/${Configuration.notAnId}")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(412)
  }

  fun deleteWithoutHeader() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", configuration.correctToken))
        .put("/v2/service_instances/${Configuration.notAnId}/service_bindings/${Configuration.notAnId}")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(412)
  }

  fun putNoAuth() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .put("/v2/service_instances/${Configuration.notAnId}/service_bindings/${Configuration.notAnId}")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
  }

  fun putWrongUser() {
    RestAssured.with()
        .header(Header("Authorization", configuration.wrongUserToken))
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", "$configuration.apiVersion"))
        .put("/v2/service_instances/${Configuration.notAnId}/service_bindings/${Configuration.notAnId}")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
  }

  fun putWrongPassword() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", configuration.wrongPasswordToken))
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .put("/v2/service_instances/${Configuration.notAnId}/service_bindings/${Configuration.notAnId}")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
  }

  fun deleteNoAuth() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .delete("/v2/service_instances/${Configuration.notAnId}/service_bindings/${Configuration.notAnId}")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
  }

  fun deleteWrongUser() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", configuration.wrongUserToken))
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .delete("/v2/service_instances/${Configuration.notAnId}/service_bindings/${Configuration.notAnId}")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
  }

  fun deleteWrongPassword() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", configuration.wrongPasswordToken))
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .delete("/v2/service_instances/${Configuration.notAnId}/service_bindings/${Configuration.notAnId}")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
  }
}