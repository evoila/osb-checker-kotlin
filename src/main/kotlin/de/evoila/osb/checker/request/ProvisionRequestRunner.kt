package de.evoila.osb.checker.request

import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.config.Configuration.token
import de.evoila.osb.checker.request.bodies.RequestBody
import de.evoila.osb.checker.response.LastOperationResponse
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.http.Header

class ProvisionRequestRunner(
    val instance_Id: String
) {

  fun runPutProvisionRequestSync(requestBody: RequestBody): Int {
    return RestAssured.with()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .header(Header("Authorization", token))
        .contentType(ContentType.JSON)
        .body(requestBody)
        .put("/v2/service_instances/$instance_Id")
        .then()
        .assertThat()
        .extract()
        .statusCode()
  }

  fun runPutProvisionRequestAsync(requestBody: RequestBody, expectedStatusCode: Int) {

    RestAssured.with()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .header(Header("Authorization", token))
        .contentType(ContentType.JSON)
        .body(requestBody)
        .put("/v2/service_instances/$instance_Id?accepts_incomplete=true")
        .then()
        .assertThat()
        .statusCode(expectedStatusCode)
  }

  fun runPatchProvisionRequest(requestBody: RequestBody, expectedStatusCode: Int) {
    RestAssured.with()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .header(Header("Authorization", token))
        .contentType(ContentType.JSON)
        .body(requestBody)
        .put("/v2/service_instances/$instance_Id?accepts_incomplete=true")
        .then()
        .assertThat()
        .statusCode(expectedStatusCode)
  }

  fun runGetLastOperation(expectedStatusCode: Int) {
    RestAssured.with()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .header(Header("Authorization", token))
        .contentType(ContentType.JSON)
        .get("/v2/service_instances/$instance_Id/last_operation")
        .then()
        .assertThat()
        .statusCode(expectedStatusCode)
  }

  fun waitForFinish(): String? {
    val response = RestAssured.with()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .header(Header("Authorization", token))
        .contentType(ContentType.JSON)
        .get("/v2/service_instances/$instance_Id/last_operation")
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

    var path = "/v2/service_instances/$instance_Id"

    path = serviceId?.let { "$path&service_id=$serviceId" } ?: path
    path = planId?.let { "$path&plan_id=$planId" } ?: path

    return RestAssured.with()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .header(Header("Authorization", token))
        .contentType(ContentType.JSON)
        .delete(path)
        .then()
        .extract()
        .statusCode()

  }

  fun runDeleteProvisionRequestAsync(serviceId: String?, planId: String?): Int {

    var path = "/v2/service_instances/$instance_Id?accepts_incomplete=true"

    path = serviceId?.let { "$path&service_id=$serviceId" } ?: path
    path = planId?.let { "$path&plan_id=$planId" } ?: path

    return RestAssured.with()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .header(Header("Authorization", token))
        .contentType(ContentType.JSON)
        .delete(path)
        .then()
        .extract()
        .statusCode()

  }
}