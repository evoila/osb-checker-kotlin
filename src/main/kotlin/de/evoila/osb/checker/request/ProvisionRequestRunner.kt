package de.evoila.osb.checker.request

import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.config.Configuration.Companion.token
import de.evoila.osb.checker.request.bodies.RequestBody
import de.evoila.osb.checker.response.LastOperationResponse
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.module.jsv.JsonSchemaValidator
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired

class ProvisionRequestRunner(
    private val instanceId: String
) {

  @Autowired
  lateinit var configuration: Configuration

  fun runPutProvisionRequestSync(requestBody: RequestBody): Int {
    return RestAssured.with()
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .header(Header("Authorization", token))
        .contentType(ContentType.JSON)
        .body(requestBody)
        .put("/v2/service_instances/$instanceId")
        .then()
        .assertThat()
        .extract()
        .statusCode()
  }

  fun runPutProvisionRequestAsync(requestBody: RequestBody, expectedStatusCode: Int) {
    val response = RestAssured.with()
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .header(Header("Authorization", token))
        .contentType(ContentType.JSON)
        .body(requestBody)
        .put("/v2/service_instances/$instanceId?accepts_incomplete=true")
        .then()
        .assertThat()
        .statusCode(expectedStatusCode)
        .extract()

    if (response.statusCode() in listOf(201, 202, 200)) {
      JsonSchemaValidator.matchesJsonSchemaInClasspath("provision-response-schema.json").matches(response.body())
    }
  }


  fun runPatchProvisionRequest(requestBody: RequestBody, expectedStatusCode: Int) {
    RestAssured.with()
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .header(Header("Authorization", token))
        .contentType(ContentType.JSON)
        .body(requestBody)
        .put("/v2/service_instances/$instanceId?accepts_incomplete=true")
        .then()
        .assertThat()
        .statusCode(expectedStatusCode)
  }

  fun runGetLastOperation(expectedStatusCode: Int) {
    val response = RestAssured.with()
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .header(Header("Authorization", token))
        .contentType(ContentType.JSON)
        .get("/v2/service_instances/$instanceId/last_operation")
        .then()
        .assertThat()
        .statusCode(expectedStatusCode)
        .extract()

    if (response.statusCode() == 200) {
      JsonSchemaValidator.matchesJsonSchemaInClasspath("polling-response-schema.json").matches(response.body())
    }
  }

  fun waitForFinish(): String? {
    val response = RestAssured.with()
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .header(Header("Authorization", token))
        .contentType(ContentType.JSON)
        .get("/v2/service_instances/$instanceId/last_operation")
        .then()
        .assertThat()
        .statusCode(200)
        .extract()
        .response()
        .jsonPath()
        .getObject("", LastOperationResponse::class.java)

    if (response.state == "in progress") {
      Thread.sleep(10000)
      return waitForFinish()
    }
    assert(response.state in listOf("succeeded", "failed"))

    return response.state
  }

  fun runDeleteProvisionRequestSync(serviceId: String?, planId: String?): Int {

    var path = "/v2/service_instances/$instanceId"

    path = serviceId?.let { "$path?service_id=$serviceId" } ?: path
    path = planId?.let { "$path&plan_id=$planId" } ?: path

    return RestAssured.with()
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .header(Header("Authorization", token))
        .contentType(ContentType.JSON)
        .delete(path)
        .then()
        .extract()
        .statusCode()
  }

  fun runDeleteProvisionRequestAsync(serviceId: String?, planId: String?): Int {

    var path = "/v2/service_instances/$instanceId?accepts_incomplete=true"

    path = serviceId?.let { "$path&service_id=$serviceId" } ?: path
    path = planId?.let { "$path&plan_id=$planId" } ?: path

    return RestAssured.with()
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .header(Header("Authorization", token))
        .contentType(ContentType.JSON)
        .delete(path)
        .then()
        .extract()
        .statusCode()
  }

  fun putWithoutHeader() {
    RestAssured.with()
        .header(Header("Authorization", token))
        .put("/v2/service_instances/$instanceId?accepts_incomplete=true")
        .then()
        .assertThat()
        .statusCode(412)
  }

  fun deleteWithoutHeader(serviceId: String, planId: String) {
    RestAssured.with()
        .header(Header("Authorization", token))
        .delete("/v2/service_instances/$instanceId?accepts_incomplete=true&service_id=$serviceId&plan_id=$planId")
        .then()
        .assertThat()
        .statusCode(412)
  }

  fun lastOperationWithoutHeader() {
    RestAssured.with()
        .header(Header("Authorization", token))
        .get("/v2/service_instances/$instanceId/last_operation")
        .then()
        .assertThat()
        .statusCode(412)
  }

  fun putNoAuth() {
    RestAssured.with()
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .contentType(ContentType.JSON)
        .put("/v2/service_instances/$instanceId?accepts_incomplete=true")
        .then()
        .assertThat()
        .statusCode(401)
        .extract()
  }

  fun deleteNoAuth() {
    RestAssured.with()
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .contentType(ContentType.JSON)
        .delete("/v2/service_instances/$instanceId?accepts_incomplete=true")
        .then()
        .assertThat()
        .statusCode(401)
        .extract()
  }

  fun lastOpNoAuth() {
    RestAssured.with()
        .header(Header("X-Broker-API-Version", configuration.apiVersion))
        .contentType(ContentType.JSON)
        .delete("/v2/service_instances/$instanceId/last_operation")
        .then()
        .assertThat()
        .statusCode(401)
        .extract()
  }


}