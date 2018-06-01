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
class ProvisionRequestRunner {

  fun runPutProvisionRequestSync(instanceId: String, requestBody: RequestBody): Int {
    return RestAssured.with()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .header(Header("Authorization", Configuration.token))
        .contentType(ContentType.JSON)
        .body(requestBody)
        .put("/v2/service_instances/$instanceId")
        .then()
        .assertThat()
        .extract()
        .statusCode()
  }

  fun runPutProvisionRequestAsync(instanceId: String, requestBody: RequestBody): Int {
    val response = RestAssured.with()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .header(Header("Authorization", Configuration.token))
        .contentType(ContentType.JSON)
        .body(requestBody)
        .put("/v2/service_instances/$instanceId?accepts_incomplete=true")
        .then()
        .assertThat()
        .extract()

    if (response.statusCode() in listOf(201, 202, 200)) {
      JsonSchemaValidator.matchesJsonSchemaInClasspath("provision-response-schema.json").matches(response.body())
    }

    return response.statusCode()
  }


  fun runPatchProvisionRequest(instanceId: String, requestBody: RequestBody, expectedStatusCode: Int) {
    RestAssured.with()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .header(Header("Authorization", Configuration.token))
        .contentType(ContentType.JSON)
        .body(requestBody)
        .put("/v2/service_instances/$instanceId?accepts_incomplete=true")
        .then()
        .assertThat()
        .statusCode(expectedStatusCode)
  }

  fun runGetLastOperation(instanceId: String, expectedStatusCode: Int) {
    val response = RestAssured.with()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .header(Header("Authorization", Configuration.token))
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

  fun waitForFinish(instanceId: String, expectedFinalStatusCode: Int): String? {
    val response = RestAssured.with()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .header(Header("Authorization", Configuration.token))
        .contentType(ContentType.JSON)
        .get("/v2/service_instances/$instanceId/last_operation")
        .then()
        .assertThat()
        .extract()
        .response()

    assertTrue("Expected StatusCode 200 or in case of a deletion 400 but was ${response.statusCode} ")
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
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .header(Header("Authorization", Configuration.token))
        .contentType(ContentType.JSON)
        .delete(path)
        .then()
        .extract()
        .statusCode()
  }

  fun runDeleteProvisionRequestAsync(instanceId: String, serviceId: String?, planId: String?): Int {

    var path = "/v2/service_instances/$instanceId?accepts_incomplete=true"

    path = serviceId?.let { "$path&service_id=$serviceId" } ?: path
    path = planId?.let { "$path&plan_id=$planId" } ?: path

    return RestAssured.with()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .header(Header("Authorization", Configuration.token))
        .contentType(ContentType.JSON)
        .delete(path)
        .then()
        .extract()
        .statusCode()
  }

  fun putWithoutHeader() {
    RestAssured.with()
        .header(Header("Authorization", Configuration.token))
        .put("/v2/service_instances/${Configuration.NOT_AN_ID}?accepts_incomplete=true")
        .then()
        .assertThat()
        .statusCode(412)
  }

  fun deleteWithoutHeader() {
    RestAssured.with()
        .header(Header("Authorization", Configuration.token))
        .delete("/v2/service_instances/${Configuration.NOT_AN_ID}?accepts_incomplete=true&service_id=Invalid&plan_id=Invalid")
        .then()
        .assertThat()
        .statusCode(412)
  }

  fun lastOperationWithoutHeader() {
    RestAssured.with()
        .header(Header("Authorization", Configuration.token))
        .get("/v2/service_instances/${Configuration.NOT_AN_ID}/last_operation")
        .then()
        .assertThat()
        .statusCode(412)
  }

  fun putNoAuth() {
    RestAssured.with()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .contentType(ContentType.JSON)
        .put("/v2/service_instances/${Configuration.NOT_AN_ID}?accepts_incomplete=true")
        .then()
        .assertThat()
        .statusCode(401)
        .extract()
  }

  fun deleteNoAuth() {
    RestAssured.with()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .contentType(ContentType.JSON)
        .delete("/v2/service_instances/${Configuration.NOT_AN_ID}?accepts_incomplete=true")
        .then()
        .assertThat()
        .statusCode(401)
        .extract()
  }

  fun lastOpNoAuth() {
    RestAssured.with()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .contentType(ContentType.JSON)
        .delete("/v2/service_instances/${Configuration.NOT_AN_ID}/last_operation")
        .then()
        .assertThat()
        .statusCode(401)
        .extract()
  }
}