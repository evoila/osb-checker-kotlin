package de.evoila.osb.checker.request

import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.request.bodies.RequestBody
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.module.jsv.JsonSchemaValidator

class BindingRequestRunner(
    private val instanceId: String,
    private val bindingId: String
) {

  fun runPutBindingRequest(requestBody: RequestBody, expectedStatusCode: Int) {

    val response = RestAssured.with()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .header(Header("Authorization", Configuration.token))
        .contentType(ContentType.JSON)
        .body(requestBody)
        .put("/v2/service_instances/$instanceId/service_bindings/$bindingId")
        .then()
        .assertThat()
        .statusCode(expectedStatusCode)
        .extract()

    if (response.statusCode() in listOf(200, 201)) {
      JsonSchemaValidator.matchesJsonSchema("binding-response-schema.json").matches(response.body())
    }
  }

  fun runDeleteBindingRequest(serviceId: String?, planId: String?, expectedStatusCode: Int) {

    var path = "/v2/service_instances/$instanceId/service_bindings/$bindingId"
    path = serviceId?.let { "$path?service_id=$serviceId" } ?: path

    path = planId?.let {
      serviceId.let { "$path&plan_id=$planId" }
      "$path?plan_id=$planId"
    } ?: path

    RestAssured.with()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .header(Header("Authorization", Configuration.token))
        .contentType(ContentType.JSON)
        .delete(path)
        .then()
        .assertThat()
        .statusCode(expectedStatusCode)
  }
}