package de.evoila.osb.checker.request

import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.request.bodies.RequestBody
import de.evoila.osb.checker.response.LastOperationResponse.*
import de.evoila.osb.checker.response.ServiceInstance
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
class ProvisionRequestRunner(
    configuration: Configuration
) : PollingRequestHandler(
    configuration
) {

  fun getProvision(instanceId: String, retrievable: Boolean): ServiceInstance {
    return RestAssured.with()
        .log().ifValidationFails()
        .headers(validHeaders)
        .contentType(ContentType.JSON)
        .get("/v2/service_instances/$instanceId")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(200)
        .and()
        .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("fetch-instance-response-schema.json"))
        .extract()
        .response()
        .jsonPath()
        .getObject("", ServiceInstance::class.java)
  }

  fun runPutProvisionRequestSync(instanceId: String, requestBody: RequestBody) {
    val response = RestAssured.with()
        .log().ifValidationFails()
        .headers(validHeaders)
        .contentType(ContentType.JSON)
        .body(requestBody)
        .put("/v2/service_instances/$instanceId")
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(IsIn(listOf(201, 422)))
        .and()
        .extract()

    if (response.statusCode() == 201) {
      JsonSchemaValidator.matchesJsonSchemaInClasspath("provision-response-schema.json").matches(response.body())
    }
  }

  fun runPutProvisionRequestAsync(instanceId: String, requestBody: RequestBody, vararg expectedFinalStatusCodes: Int)
      : ExtractableResponse<Response> {
    val response = RestAssured.with()
        .log().ifValidationFails()
        .headers(validHeaders)
        .contentType(ContentType.JSON)
        .body(requestBody)
        .put("/v2/service_instances/$instanceId?accepts_incomplete=true")
        .then()
        .log().ifValidationFails()
        .statusCode(IsIn(expectedFinalStatusCodes.asList()))
        .assertThat()
        .extract()

    if (response.statusCode() in listOf(201, 202, 200)) {
      JsonSchemaValidator.matchesJsonSchemaInClasspath("provision-response-schema.json").matches(response.body())
    }

    return response
  }

  fun polling(instanceId: String, expectedFinalStatusCode: Int, operationData: String, maxPollingDuration: Int): State {
    val latestAcceptablePollingInstant = Instant.now().plusSeconds(maxPollingDuration.toLong())

    return super.waitForFinish(path = "/v2/service_instances/$instanceId/last_operation",
        expectedFinalStatusCode = expectedFinalStatusCode,
        operationData = operationData,
        latestAcceptablePollingInstant = latestAcceptablePollingInstant
    )
  }

  fun runDeleteProvisionRequestSync(instanceId: String, serviceId: String?, planId: String?) {
    var path = "/v2/service_instances/$instanceId"
    path = serviceId?.let { "$path?service_id=$serviceId" } ?: path
    path = planId?.let { "$path&plan_id=$planId" } ?: path
    RestAssured.with()
        .log().ifValidationFails()
        .headers(validHeaders)
        .contentType(ContentType.JSON)
        .delete(path)
        .then()
        .log().ifValidationFails()
        .statusCode(IsIn(listOf(200, 422)))
        .extract()
  }

  fun runDeleteProvisionRequestAsync(instanceId: String,
                                     serviceId: String?,
                                     planId: String?,
                                     expectedFinalStatusCodes: IntArray): ExtractableResponse<Response> {
    var path = "/v2/service_instances/$instanceId?accepts_incomplete=true"
    path = serviceId?.let { "$path&service_id=$serviceId" } ?: path
    path = planId?.let { "$path&plan_id=$planId" } ?: path

    return RestAssured.with()
        .log().ifValidationFails()
        .headers(validHeaders)
        .contentType(ContentType.JSON)
        .delete(path)
        .then()
        .log().ifValidationFails()
        .assertThat()
        .statusCode(IsIn(expectedFinalStatusCodes.asList()))
        .extract()
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