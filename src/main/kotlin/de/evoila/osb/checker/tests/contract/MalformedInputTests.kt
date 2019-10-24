package de.evoila.osb.checker.tests.contract

import de.evoila.osb.checker.request.ProvisionRequestRunner
import de.evoila.osb.checker.request.bodies.ProvisionBody
import de.evoila.osb.checker.tests.TestBase
import de.evoila.osb.checker.tests.containers.BindingContainerService
import org.junit.jupiter.api.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

@DisplayName("Checking service-broker behaviour when using strings outside of common/best practice.")
class MalformedInputTests : TestBase() {

    @Autowired
    lateinit var provisionRequestRunner: ProvisionRequestRunner
    @Autowired
    lateinit var bindingContaines: BindingContainerService


    @Test
    @DisplayName("Testing behaviour when values contain characters outside of \"Unreserved Characters\" are used.")
    fun testCatalogEndpointWithMalformedHeaderValues() {
        if (configuration.apiVersion >= 2.15) {
            malformedCatalogHeader("X-Broker-API-Request-Identity")
        }
        if (configuration.apiVersion >= 1.14) {
            malformedCatalogHeader("X-Broker-API-Originating-Identity")
        }
    }

    @TestFactory
    @DisplayName("Trying to create a service instance and binding with unicode-symbols as identifier")
    fun testsProvisionEndpointsWithMalformedPathVariables(): List<DynamicNode> {
        val catalog = configuration.initCustomCatalog(catalogRequestRunner.correctRequest())
        return catalog.services.flatMap { service ->
            service.plans.map { plan ->
                DynamicTest.dynamicTest("Testing PUT and Delete instance for '${service.name}' plan '${plan.name}'") {
                    val strangeInstanceId = createStrangeId()
                    val response = provisionRequestRunner.runPutProvisionRequestAsyncWithoutValidation(
                            instanceId = strangeInstanceId,
                            requestBody = ProvisionBody.ValidProvisioning(
                                    service_id = service.id,
                                    plan_id = plan.id,
                                    maintenance_info = if (configuration.apiVersion >= 2.15) {
                                        plan.maintenanceInfo
                                    } else {
                                        null
                                    }
                            ).apply { setContextUpdate(configuration.contextObjectType) }
                    )
                    if (response.statusCode() in 200..299) {
                        log.warn("Creation of InstanceId's like $strangeInstanceId are accepted. This can lead to malformed URIs if they are used in dashboard URLs. Consider blocking this.")
                    }
                }
            }
        }
    }

    private fun malformedCatalogHeader(headerKey: String) {
        val value = createStrangeId()
        val response = catalogRequestRunner.withSpecificHeaderAndNoAssertion(headerKey, value)
        if (response.statusCode() == 200) {
            log.warn("The catalog endpoint accepts $headerKey with values like $value. It may be useful to block such headers.")
        }
    }

    private fun createStrangeId(): String {
        var strangeId = ""
        (0..35).map { uniCodeSymbols.random() }.forEach { strangeId += it }
        return strangeId
    }

    companion object {
        private val genDelims = arrayOf(":", "/", "?", "#", "[", "]", "@")
        private val subDelims = arrayOf("*", "+", ",", ";", "=")
        private val uniCodeSymbols = 123.toChar()..65535.toChar()
        private val log = LoggerFactory.getLogger(MalformedInputTests::class.java)
    }
}