package de.evoila.obs.checker.tests

import de.evoila.obs.checker.Application
import de.evoila.obs.checker.request.bodies.ProvisionRequestBody
import de.evoila.obs.checker.util.Performance
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.http.Header
import org.slf4j.LoggerFactory
import java.util.*

class ProvisionTests(
    private val scenario: ProvisionTests.Scenario,
    private val provisionRequestBody: ProvisionRequestBody.Valid,
    private val token: String
) {

  fun runAll() {
    missingServiceId()
    missingPlanId()
    invalidServiceId()
    invalidPlanId()
    invalidSchema()

    when (scenario) {
      Scenario.NEW -> newScenario()
      Scenario.CONFLICT -> conflictScenario()
      Scenario.UPDATE -> updateScenario()
    }
  }

  private fun updateScenario() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  private fun conflictScenario() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  private fun newScenario() {
    Performance.logTime(log, "PROVISION - new") {
      validProvisionRequest(provisionRequestBody)
      validLastOperationStatus()
      runDeleteProvisionRequest(
          provisionRequestBody.service_id!!,
          provisionRequestBody.plan_id!!,
          202)
    }
  }

  private fun missingServiceId() {
    Performance.logTime(log, "should reject if missing service_id") {
      val missingServiceID = ProvisionRequestBody.Valid(
          service_id = null,
          plan_id = provisionRequestBody.plan_id
      )
      runPutProvisionRequest(
          missingServiceID,
          400)
    }
  }

  private fun missingPlanId() {
    Performance.logTime(log, "should reject if missing plan_id") {
      val missingPlanId = ProvisionRequestBody.Valid(
          service_id = provisionRequestBody.service_id,
          plan_id = null
      )

      runPutProvisionRequest(missingPlanId, 400)
    }
  }

  private fun invalidServiceId() {
    Performance.logTime(log, "should reject if service_id is invalid") {
      val invalidServiceId = ProvisionRequestBody.Valid(
          service_id = "Invalid",
          plan_id = provisionRequestBody.plan_id
      )

      runPutProvisionRequest(invalidServiceId, 400)
    }
  }

  private fun invalidPlanId() {
    Performance.logTime(log, "should reject if plan_id is invalid") {
      val invalidPlanId = ProvisionRequestBody.Valid(
          service_id = provisionRequestBody.service_id,
          plan_id = "Invalid"
      )

      runPutProvisionRequest(invalidPlanId, 400)
    }
  }

  private fun invalidSchema() {
    Performance.logTime(log, "should reject if parameters are not following schema") {
      runPutProvisionRequest(ProvisionRequestBody.Invalid(), 400)
    }
  }

  private fun validProvisionRequest(provisionRequestBody: ProvisionRequestBody) {
    Performance.logTime(log, "should accept a valid provision request") {
      runPutProvisionRequest(provisionRequestBody, 202)
    }
  }

  private fun runPutProvisionRequest(provisionRequestBody: ProvisionRequestBody, expectedStatusCode: Int) {
    RestAssured.with()
        .header(Header("X-Broker-API-Version", Application.apiVersion))
        .header(Header("Authorization", token))
        .contentType(ContentType.JSON)
        .body(provisionRequestBody)
        .put("/v2/service_instances/$INSTANCE_ID?accepts_incomplete=true")
        .then()
        .assertThat()
        .statusCode(expectedStatusCode)
  }

  private fun runPatchProvisionRequest(provisionRequestBody: ProvisionRequestBody, expectedStatusCode: Int) {
    RestAssured.with()
        .header(Header("X-Broker-API-Version", Application.apiVersion))
        .header(Header("Authorization", token))
        .contentType(ContentType.JSON)
        .body(provisionRequestBody)
        .put("/v2/service_instances/$INSTANCE_ID?accepts_incomplete=true")
        .then()
        .assertThat()
        .statusCode(expectedStatusCode)
  }

  fun validLastOperationStatus() {
    runGetLastOperation(200)
  }

  private fun runGetLastOperation(expectedStatusCode: Int) {
    RestAssured.with()
        .header(Header("X-Broker-API-Version", Application.apiVersion))
        .header(Header("Authorization", token))
        .contentType(ContentType.JSON)
        .body(provisionRequestBody)
        .get("/v2/service_instances/$INSTANCE_ID/last_operation")
        .then()
        .assertThat()
        .statusCode(expectedStatusCode)
  }

  private fun runDeleteProvisionRequest(serviceId: String, planId: String, expectedStatusCode: Int) {

    RestAssured.with()
        .header(Header("X-Broker-API-Version", Application.apiVersion))
        .header(Header("Authorization", token))
        .contentType(ContentType.JSON)
        .body(provisionRequestBody)
        .delete("/v2/service_instances/$INSTANCE_ID?accepts_incomplete=true&plan_id=$planId&service_id=$serviceId")
        .then()
        .assertThat()
        .statusCode(expectedStatusCode)
  }

  enum class Scenario {
    NEW,
    UPDATE,
    CONFLICT
  }

  companion object {
    private val INSTANCE_ID = UUID.randomUUID().toString()
    private val log = LoggerFactory.getLogger(ProvisionTests::class.java)
  }
}