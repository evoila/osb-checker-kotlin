package de.evoila.osb.checker.request

import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.request.bodies.RequestBody
import de.evoila.osb.checker.response.LastOperationResponse
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.module.jsv.JsonSchemaValidator
import org.springframework.stereotype.Service
import kotlin.test.assertTrue

@Service
class BindingRequestRunner(
    val configuration: Configuration
) {

  fun runGetBindingRequest(expectedStatusCode: Int, instanceId: String, bindingId: String) {

    val response = RestAssured.with()
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .header(Header("Authorization", configuration.correctToken))
        .contentType(ContentType.JSON)
        .get("/v2/service_instances/$instanceId/service_bindings/$bindingId")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(expectedStatusCode)
        .extract()

    JsonSchemaValidator.matchesJsonSchemaInClasspath("fetch-binding-response-schema.json").matches(response.body())
  }

  fun runPutBindingRequest(requestBody: RequestBody, instanceId: String, bindingId: String): Int {

    val response = RestAssured.with()
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .header(Header("Authorization", configuration.correctToken))
        .contentType(ContentType.JSON)
        .body(requestBody)
        .param("accepts_incomplete", true)
        .put("/v2/service_instances/$instanceId/service_bindings/$bindingId")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .extract()

    if (response.statusCode() in listOf(200, 201)) {
      JsonSchemaValidator.matchesJsonSchemaInClasspath("binding-response-schema.json").matches(response.body())
    }

    return response.statusCode()
  }

  fun waitForFinish(instanceId: String, bindingId: String, expectedFinalStatusCode: Int): String {
    val response = RestAssured.with()
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .header(Header("Authorization", configuration.correctToken))
        .contentType(ContentType.JSON)
        .get("/v2/service_instances/$instanceId/service_bindings/$bindingId/last_operation")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .extract()
        .response()

    assertTrue("Expected StatusCode is $expectedFinalStatusCode but was ${response.statusCode} ")
    { response.statusCode in listOf(expectedFinalStatusCode, 200) }

    return if (response.statusCode == 200) {

      val responseBody = response.jsonPath()
          .getObject("", LastOperationResponse::class.java)

      JsonSchemaValidator.matchesJsonSchemaInClasspath("polling-response-schema.json").matches(responseBody)

      if (responseBody.state == "in progress") {
        Thread.sleep(10000)
        return waitForFinish(instanceId, bindingId, expectedFinalStatusCode)
      }
      assertTrue("Expected response body \"succeeded\" or \"failed\" but was ${responseBody.state}")
      { responseBody.state in listOf("succeeded", "failed") }

      responseBody.state
    } else {
      ""
    }
  }

  fun runDeleteBindingRequest(serviceId: String?, planId: String?, instanceId: String, bindingId: String): Int {

    var path = "/v2/service_instances/$instanceId/service_bindings/$bindingId"
    path = serviceId?.let { "$path?service_id=$serviceId" } ?: path

    return RestAssured.with()
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .header(Header("Authorization", configuration.correctToken))
        .contentType(ContentType.JSON)
        .param("service_id", serviceId)
        .param("plan_id", planId)
        .param("accepts_incomplete", true)
        .delete(path)
        .then()
        .log().ifValidationFails()
        .assertThat()
        .extract()
        .response().statusCode
  }

  fun putWithoutHeader() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", configuration.correctToken))
        .put("/v2/service_instances/${Configuration.NOT_AN_ID}/service_bindings/${Configuration.NOT_AN_ID}")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(412)
  }

  fun deleteWithoutHeader() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", configuration.correctToken))
        .put("/v2/service_instances/${Configuration.NOT_AN_ID}/service_bindings/${Configuration.NOT_AN_ID}")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(412)
  }

  fun putNoAuth() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .put("/v2/service_instances/${Configuration.NOT_AN_ID}/service_bindings/${Configuration.NOT_AN_ID}")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
  }

  fun putWrongUser() {
    RestAssured.with()
        .header(Header("Authorization", configuration.wrongUserToken))
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .put("/v2/service_instances/${Configuration.NOT_AN_ID}/service_bindings/${Configuration.NOT_AN_ID}")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
  }

  fun putWrongPassword() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", configuration.wrongPasswordToken))
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .put("/v2/service_instances/${Configuration.NOT_AN_ID}/service_bindings/${Configuration.NOT_AN_ID}")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
  }


  fun deleteNoAuth() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .delete("/v2/service_instances/${Configuration.NOT_AN_ID}/service_bindings/${Configuration.NOT_AN_ID}")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
  }

  fun deleteWrongUser() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", configuration.wrongUserToken))
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .delete("/v2/service_instances/${Configuration.NOT_AN_ID}/service_bindings/${Configuration.NOT_AN_ID}")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
  }

  fun deleteWrongPassword() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", configuration.wrongPasswordToken))
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .delete("/v2/service_instances/${Configuration.NOT_AN_ID}/service_bindings/${Configuration.NOT_AN_ID}")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
  }
}