package de.evoila.osb.checker.request

import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.request.bodies.RequestBody
import de.evoila.osb.checker.response.LastOperationResponse
import de.evoila.osb.checker.response.ServiceInstance
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.module.jsv.JsonSchemaValidator
import org.springframework.stereotype.Service
import kotlin.test.assertTrue

@Service
class ProvisionRequestRunner(
    val configuration: Configuration
) {

  fun getProvision(instanceId: String, retrievable: Boolean): ServiceInstance? {
    val response = RestAssured.with()
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .header(Header("Authorization", configuration.correctToken))
        .contentType(ContentType.JSON)
        .get("/v2/service_instances/$instanceId")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .extract()
        .response()

    return if (retrievable) {
      assertTrue("Expected StatusCode is 200 but was ${response.statusCode}") { response.statusCode == 200 }

      JsonSchemaValidator.matchesJsonSchemaInClasspath("polling-response-schema.json").matches(response.body)

      return response.jsonPath().getObject("", ServiceInstance::class.java)
    } else {

      null
    }
  }

  fun runPutProvisionRequestSync(instanceId: String, requestBody: RequestBody): Int {
    return RestAssured.with()
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .header(Header("Authorization", configuration.correctToken))
        .contentType(ContentType.JSON)
        .body(requestBody)
        .put("/v2/service_instances/$instanceId")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .extract()
        .statusCode()
  }

  fun runPutProvisionRequestAsync(instanceId: String, requestBody: RequestBody): Int {
    val response = RestAssured.with()
        .log().all()
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .header(Header("Authorization", configuration.correctToken))
        .contentType(ContentType.JSON)
        .body(requestBody)
        .put("/v2/service_instances/$instanceId?accepts_incomplete=true")
        .then()
        .log().all()
        .assertThat()
        .extract()

    if (response.statusCode() in listOf(201, 202, 200)) {
      JsonSchemaValidator.matchesJsonSchemaInClasspath("provision-response-schema.json").matches(response.body())
    }

    return response.statusCode()
  }

  fun waitForFinish(instanceId: String, expectedFinalStatusCode: Int): String {
    val response = RestAssured.with()
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .header(Header("Authorization", configuration.correctToken))
        .contentType(ContentType.JSON)
        .get("/v2/service_instances/$instanceId/last_operation")
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
        return waitForFinish(instanceId, expectedFinalStatusCode)
      }
      assertTrue("Expected response body \"succeeded\" or \"failed\" but was ${responseBody.state}")
      { responseBody.state in listOf("succeeded", "failed") }

      responseBody.state
    } else {
      ""
    }
  }

  fun runDeleteProvisionRequestSync(instanceId: String, serviceId: String?, planId: String?): Int {

    var path = "/v2/service_instances/$instanceId"

    path = serviceId?.let { "$path?service_id=$serviceId" } ?: path
    path = planId?.let { "$path&plan_id=$planId" } ?: path

    return RestAssured.with()
        .log().all()
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .header(Header("Authorization", configuration.correctToken))
        .contentType(ContentType.JSON)
        .delete(path)
        .then()
        .log().all()
        .extract()
        .statusCode()
  }

  fun runDeleteProvisionRequestAsync(instanceId: String, serviceId: String?, planId: String?): Int {

    var path = "/v2/service_instances/$instanceId?accepts_incomplete=true"

    path = serviceId?.let { "$path&service_id=$serviceId" } ?: path
    path = planId?.let { "$path&plan_id=$planId" } ?: path

    return RestAssured.with()
        .log().all()
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .header(Header("Authorization", configuration.correctToken))
        .contentType(ContentType.JSON)
        .delete(path)
        .then()
        .log().all  ()
        .extract()
        .statusCode()
  }

  fun putWithoutHeader() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", configuration.correctToken))
        .put("/v2/service_instances/${Configuration.notAnId}?accepts_incomplete=true")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(412)
  }

  fun deleteWithoutHeader() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", configuration.correctToken))
        .delete("/v2/service_instances/${Configuration.notAnId}?accepts_incomplete=true&service_id=Invalid&plan_id=Invalid")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(412)
  }

  fun lastOperationWithoutHeader() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", configuration.correctToken))
        .get("/v2/service_instances/${Configuration.notAnId}/last_operation")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(412)
  }

  fun putNoAuth() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .contentType(ContentType.JSON)
        .put("/v2/service_instances/${Configuration.notAnId}?accepts_incomplete=true")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
        .extract()
  }

  fun putWrongUser() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", configuration.wrongUserToken))
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .contentType(ContentType.JSON)
        .put("/v2/service_instances/${Configuration.notAnId}?accepts_incomplete=true")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
        .extract()
  }

  fun putWrongPassword() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", configuration.wrongPasswordToken))
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .contentType(ContentType.JSON)
        .put("/v2/service_instances/${Configuration.notAnId}?accepts_incomplete=true")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
        .extract()
  }

  fun deleteNoAuth() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .contentType(ContentType.JSON)
        .delete("/v2/service_instances/${Configuration.notAnId}?accepts_incomplete=true")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
        .extract()
  }

  fun deleteWrongUser() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", configuration.wrongUserToken))
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .contentType(ContentType.JSON)
        .delete("/v2/service_instances/${Configuration.notAnId}?accepts_incomplete=true")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
        .extract()
  }

  fun deleteWrongPassword() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", configuration.wrongPasswordToken))
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .contentType(ContentType.JSON)
        .delete("/v2/service_instances/${Configuration.notAnId}?accepts_incomplete=true")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
        .extract()
  }


  fun lastOpNoAuth() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .contentType(ContentType.JSON)
        .get("/v2/service_instances/${Configuration.notAnId}/last_operation")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
        .extract()
  }

  fun lastOpWrongUser() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", configuration.wrongUserToken))
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .contentType(ContentType.JSON)
        .get("/v2/service_instances/${Configuration.notAnId}/last_operation")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
        .extract()
  }

  fun lastOpWrongPassword() {
    RestAssured.with()
        .log().ifValidationFails()
        .header(Header("Authorization", configuration.wrongPasswordToken))
        .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
        .contentType(ContentType.JSON)
        .get("/v2/service_instances/${Configuration.notAnId}/last_operation")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(401)
        .extract()
  }
}