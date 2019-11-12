package de.evoila.osb.checker.config

import io.restassured.RestAssured
import org.springframework.stereotype.Service
import java.util.*
import kotlin.test.assertTrue

@Service
class RestAssureConfig(
        configuration: Configuration
) {
    init {
        assertTrue(configuration.apiVersion in supportedApiVersions, noSupportedApiVersion)

        RestAssured.baseURI = configuration.url
        RestAssured.port = configuration.port
        if (configuration.skipTLSVerification) {
            RestAssured.useRelaxedHTTPSValidation()
        }
    }

    companion object {
        private val supportedApiVersions = listOf(2.13, 2.14, 2.15)
        private val noSupportedApiVersion = "You entered a not supported Api Version. Please use one of the following:" +
                " $supportedApiVersions"
    }
}