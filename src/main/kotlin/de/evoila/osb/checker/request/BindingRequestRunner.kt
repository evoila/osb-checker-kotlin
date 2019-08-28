package de.evoila.osb.checker.request

import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.request.bodies.RequestBody
import de.evoila.osb.checker.response.operations.LastOperationResponse
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
class BindingRequestRunner(
        configuration: Configuration
) : PollingRequestHandler(
        configuration
) {

    fun runGetBindingRequest(expectedStatusCode: Int, instanceId: String, bindingId: String) {
        val response = RestAssured.with()
                .log().ifValidationFails()
                .headers(validRequestHeaders)
                .contentType(ContentType.JSON)
                .get(SERVICE_INSTANCE_PATH + instanceId + SERVICE_BINDING_PATH + bindingId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .headers(expectedResponseHeaders)
                .statusCode(expectedStatusCode)
                .extract()

        JsonSchemaValidator.matchesJsonSchemaInClasspath("fetch-binding-response-schema.json")
                .matches(response.body())
    }

    fun runPutBindingRequest(requestBody: RequestBody,
                             instanceId: String,
                             bindingId: String,
                             vararg expectedStatusCodes: Int): ExtractableResponse<Response> {
        val response = RestAssured.with()
                .log().ifValidationFails()
                .headers(validRequestHeaders)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .param("accepts_incomplete", configuration.apiVersion > 2.13)
                .put(SERVICE_INSTANCE_PATH + instanceId + SERVICE_BINDING_PATH + bindingId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .headers(expectedResponseHeaders)
                .statusCode(IsIn(expectedStatusCodes.asList()))
                .extract()

        if (response.statusCode() == 200) {
            JsonSchemaValidator.matchesJsonSchemaInClasspath("binding-response-schema.json")
                    .matches(response.body())
        }

        return response
    }

    fun polling(instanceId: String,
                bindingId: String,
                expectedFinalStatusCode: Int,
                operationData: String?,
                maxPollingDuration: Int): LastOperationResponse.State {
        val latestAcceptablePollingInstant = Instant.now().plusSeconds(maxPollingDuration.toLong())

        return waitForFinish(
                path = SERVICE_INSTANCE_PATH + instanceId + SERVICE_BINDING_PATH + bindingId + LAST_OPERATION,
                expectedFinalStatusCode = expectedFinalStatusCode,
                operationData = operationData,
                latestAcceptablePollingInstant = latestAcceptablePollingInstant)
    }

    fun runDeleteBindingRequest(serviceId: String?,
                                planId: String?,
                                instanceId: String,
                                bindingId: String, vararg expectedStatusCodes: Int): ExtractableResponse<Response> {
        var path = SERVICE_INSTANCE_PATH + instanceId + SERVICE_BINDING_PATH + bindingId
        path = serviceId?.let { "$path?service_id=$serviceId" } ?: path

        return RestAssured.with()
                .log().ifValidationFails()
                .headers(validRequestHeaders)
                .contentType(ContentType.JSON)
                .param("service_id", serviceId)
                .param("plan_id", planId)
                .param("accepts_incomplete", configuration.apiVersion > 2.13)
                .delete(path)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .headers(expectedResponseHeaders)
                .statusCode(IsIn(expectedStatusCodes.asList()))
                .extract()
    }

    fun putWithoutHeader() {
        RestAssured.with()
                .log().ifValidationFails()
                .header(Header("Authorization", configuration.correctToken))
                .put(SERVICE_INSTANCE_PATH + Configuration.notAnId + SERVICE_BINDING_PATH + Configuration.notAnId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(412)
    }

    fun deleteWithoutHeader() {
        RestAssured.with()
                .log().ifValidationFails()
                .header(Header("Authorization", configuration.correctToken))
                .put(SERVICE_INSTANCE_PATH + Configuration.notAnId + SERVICE_BINDING_PATH + Configuration.notAnId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(412)
    }

    fun putNoAuth() {
        RestAssured.with()
                .log().ifValidationFails()
                .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
                .put(SERVICE_INSTANCE_PATH + Configuration.notAnId + SERVICE_BINDING_PATH + Configuration.notAnId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(401)
    }

    fun putWrongUser() {
        RestAssured.with()
                .header(Header("Authorization", configuration.wrongUserToken))
                .log().ifValidationFails()
                .header(Header("X-Broker-API-Version", "$configuration.apiVersion"))
                .put(SERVICE_INSTANCE_PATH + Configuration.notAnId + SERVICE_BINDING_PATH + Configuration.notAnId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(401)
    }

    fun putWrongPassword() {
        RestAssured.with()
                .log().ifValidationFails()
                .header(Header("Authorization", configuration.wrongPasswordToken))
                .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
                .put(SERVICE_INSTANCE_PATH + Configuration.notAnId + SERVICE_BINDING_PATH + Configuration.notAnId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(401)
    }

    fun deleteNoAuth() {
        RestAssured.with()
                .log().ifValidationFails()
                .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
                .delete(SERVICE_INSTANCE_PATH + Configuration.notAnId + SERVICE_BINDING_PATH + Configuration.notAnId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(401)
    }

    fun deleteWrongUser() {
        RestAssured.with()
                .log().ifValidationFails()
                .header(Header("Authorization", configuration.wrongUserToken))
                .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
                .delete(SERVICE_INSTANCE_PATH + Configuration.notAnId + SERVICE_BINDING_PATH + Configuration.notAnId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(401)
    }

    fun deleteWrongPassword() {
        RestAssured.with()
                .log().ifValidationFails()
                .header(Header("Authorization", configuration.wrongPasswordToken))
                .header(Header("X-Broker-API-Version", "${configuration.apiVersion}"))
                .delete(SERVICE_INSTANCE_PATH + Configuration.notAnId + SERVICE_BINDING_PATH + Configuration.notAnId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(401)
    }

    companion object {
        const val SERVICE_BINDING_PATH = "/service_bindings/"
    }
}