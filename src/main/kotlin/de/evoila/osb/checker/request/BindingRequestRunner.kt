package de.evoila.osb.checker.request

import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.request.bodies.RequestBody
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.http.Header

object BindingRequestRunner {

  fun runPutBindingRequest(requestBody: RequestBody, expectedStatusCode: Int) {
    RestAssured.with()
        .header(Header("X-Broker-API-Version", Configuration.apiVersion))
        .header(Header("Authorization", Configuration.token))
        .contentType(ContentType.JSON)
        .body(requestBody)
        .put("/v2/service_instances/${Configuration.INSTANCE_ID}/service_bindings/${Configuration.BINDING_ID}")
        .then()
        .assertThat()
        .statusCode(expectedStatusCode)
  }

  fun runDeleteBindingRequest(serviceId : String?, planId : String?, expectedStatusCode: Int) {

    var path = "/v2/service_instances/${Configuration.INSTANCE_ID}/service_bindings/${Configuration.BINDING_ID}"
    path = serviceId?.let { "$path?service_id=$serviceId" } ?: path
    path = planId?.let { "$path&plan_id=$planId" } ?: path

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