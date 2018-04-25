package de.evoila.obs.checker.tests

import de.evoila.obs.checker.Application
import de.evoila.obs.checker.request.bodies.ProvisionRequestBody
import de.evoila.obs.checker.util.Performance
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.http.Header
import org.slf4j.Logger

object ProvisionTests {

  fun runAll(log: Logger, token: String, provisionRequestBody: ProvisionRequestBody) {
    missingServiceId(log, token)
    missingPlanId(log, token)
    invalidServiceId(log, token)
    invalidPlanId(log, token)
    invalidSchema(log, token)

  }

  private fun missingServiceId(log: Logger, token: String) {
    Performance.logTime(log, "should reject if missing service_id") {
      runPutProvisionRequest(token, ProvisionRequestBody("", ""), 400)
    }
  }

  private fun missingPlanId(log: Logger, token: String) {
    Performance.logTime(log, "should reject if missing plan_id") {
      runPutProvisionRequest(token, ProvisionRequestBody("", ""), 400)
    }
  }

  private fun invalidServiceId(log: Logger, token: String) {
    Performance.logTime(log, "should reject if service_id is invalid") {
      runPutProvisionRequest(token, ProvisionRequestBody("", ""), 400)
    }
  }

  private fun invalidPlanId(log: Logger, token: String) {
    Performance.logTime(log, "should reject if plan_id is invalid") {
      runPutProvisionRequest(token, ProvisionRequestBody("", ""), 400)
    }
  }

  private fun invalidSchema(log: Logger, token: String) {
    Performance.logTime(log, "should reject if parameters are not following schema") {
      runPutProvisionRequest(token, ProvisionRequestBody("", ""), 400)
    }
  }


  private fun runPutProvisionRequest(token: String, provisionRequestBody: ProvisionRequestBody, expectedStatusCode: Int) {
    RestAssured.with()
        .header(Header("X-Broker-API-Version", Application.apiVersion))
        .header(Header("Authorization", token))
        .contentType(ContentType.JSON)
        .body(provisionRequestBody)
        .put("/v2/service_instances/instance_id")
        .then()
        .assertThat()
        .statusCode(expectedStatusCode)
  }

  private fun runPatchProvisionRequest(token: String, provisionRequestBody: ProvisionRequestBody, expectedStatusCode: Int) {
    RestAssured.with()
        .header(Header("X-Broker-API-Version", Application.apiVersion))
        .header(Header("Authorization", token))
        .contentType(ContentType.JSON)
        .body(provisionRequestBody)
        .put("/v2/service_instances/instance_id")
        .then()
        .assertThat()
        .statusCode(expectedStatusCode)
  }
}