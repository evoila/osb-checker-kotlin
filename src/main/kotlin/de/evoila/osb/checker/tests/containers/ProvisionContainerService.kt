package de.evoila.osb.checker.tests.containers

import de.evoila.osb.checker.tests.objects.Messages.EXPECTED_FINAL_POLLING_STATE
import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.request.ProvisionRequestRunner
import de.evoila.osb.checker.request.ResponseBodyType
import de.evoila.osb.checker.request.bodies.ProvisionBody
import de.evoila.osb.checker.response.catalog.Plan
import de.evoila.osb.checker.response.operations.AsyncResponse
import de.evoila.osb.checker.response.operations.LastOperationResponse
import de.evoila.osb.checker.response.operations.ProvisionResponse
import de.evoila.osb.checker.tests.objects.Messages.DELETE_PROVISION_MESSAGE
import de.evoila.osb.checker.tests.objects.Messages.DELETE_RESULT_MESSAGE
import de.evoila.osb.checker.tests.objects.Messages.TEST_DASHBOARD_DISPLAY_NAME
import de.evoila.osb.checker.tests.objects.Messages.VALID_FETCH_PROVISION
import de.evoila.osb.checker.tests.objects.Messages.VALID_PROVISION_DISPLAY_NAME
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicTest
import org.springframework.stereotype.Service
import java.util.*
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Service
class ProvisionContainerService(
        val provisionRequestRunner: ProvisionRequestRunner,
        val configuration: Configuration
) {

    fun validProvisionContainer(
            instanceId: String,
            plan: Plan,
            provision: ProvisionBody.ValidProvisioning,
            isRetrievable: Boolean,
            serviceName: String,
            planName: String
    ): DynamicContainer {
        val provisionTests = createValidProvisionTests(instanceId, provision, plan, serviceName, planName)
        var displayName = VALID_PROVISION_DISPLAY_NAME
        if (configuration.testDashboard) {
            displayName += TEST_DASHBOARD_DISPLAY_NAME
        }

        displayName += if (isRetrievable) {
            provisionTests.plus(validRetrievableInstanceContainer(instanceId, provision, isRetrievable))
            VALID_FETCH_PROVISION
        } else {
            ""
        }

        return DynamicContainer.dynamicContainer("$displayName.", provisionTests)
    }

    fun validDeleteProvisionContainer(
            instanceId: String,
            service: de.evoila.osb.checker.response.catalog.Service,
            plan: Plan
    ): DynamicContainer {
        return DynamicContainer.dynamicContainer("Deleting provision",
                listOf(
                        DynamicTest.dynamicTest(DELETE_PROVISION_MESSAGE) {
                            val response = provisionRequestRunner.runDeleteProvisionRequestAsync(
                                    instanceId = instanceId,
                                    serviceId = service.id,
                                    planId = plan.id,
                                    expectedFinalStatusCodes = intArrayOf(200, 202)
                            )

                            if (response.statusCode() == 202) {
                                val provision = response.jsonPath().getObject("", AsyncResponse::class.java)
                                assertTrue(DELETE_RESULT_MESSAGE) {
                                    LastOperationResponse.State.GONE == provisionRequestRunner.polling(
                                            instanceId = instanceId,
                                            expectedFinalStatusCode = 410,
                                            operationData = provision.operation,
                                            maxPollingDuration = plan.maximumPollingDuration
                                    )
                                }
                            }
                        },
                        DynamicTest.dynamicTest("Running valid DELETE provision with same parameters again. Expecting Status 410.") {
                            provisionRequestRunner.runDeleteProvisionRequestAsync(
                                    instanceId = instanceId,
                                    serviceId = service.id,
                                    planId = plan.id,
                                    expectedFinalStatusCodes = intArrayOf(410)
                            )
                        }
                )
        )
    }

    private fun createValidProvisionTests(
            instanceId: String,
            provision: ProvisionBody.ValidProvisioning,
            plan: Plan,
            serviceName: String,
            planName: String
    ): List<DynamicTest> {

        return listOf(
                DynamicTest.dynamicTest("Running valid PUT provision with instanceId $instanceId" +
                        " for service '$serviceName'" +
                        " and plan '$planName'") {
                    val response = provisionRequestRunner.runPutProvisionRequestAsync(
                            instanceId = instanceId,
                            requestBody = provision,
                            expectedFinalStatusCodes = *intArrayOf(201, 202, 200),
                            expectedResponseBodyType = ResponseBodyType.VALID_PROVISION
                    )

                    if (response.statusCode() == 202) {
                        val asyncResponse = response.jsonPath().getObject("", AsyncResponse::class.java)
                        val state = provisionRequestRunner.polling(
                                instanceId = instanceId,
                                expectedFinalStatusCode = 200,
                                operationData = asyncResponse.operation,
                                maxPollingDuration = plan.maximumPollingDuration
                        )
                        assertTrue(EXPECTED_FINAL_POLLING_STATE + state)
                        { LastOperationResponse.State.SUCCEEDED == state }
                        if (configuration.testDashboard && !asyncResponse.dashboardUrl.isNullOrEmpty()) {
                            provisionRequestRunner.testDashboardURL(asyncResponse.dashboardUrl)
                        }
                    } else if (configuration.testDashboard) {
                        val provisionResponse = response.jsonPath().getObject("", ProvisionResponse::class.java)
                        if (!provisionResponse.dashboardUrl.isNullOrEmpty()) {
                            provisionRequestRunner.testDashboardURL(provisionResponse.dashboardUrl)
                        }
                    }
                },
                DynamicTest.dynamicTest("Running valid PUT provision with same attributes again. Expecting Status 200.") {
                    provisionRequestRunner.runPutProvisionRequestAsync(
                            instanceId = instanceId,
                            requestBody = provision,
                            expectedFinalStatusCodes = *intArrayOf(200),
                            expectedResponseBodyType = ResponseBodyType.VALID_PROVISION
                    )
                },
                DynamicTest.dynamicTest("Running valid PUT provision with different space_guid and organization_guid. Expecting Status 409.") {
                    provisionRequestRunner.runPutProvisionRequestAsync(
                            instanceId = instanceId,
                            requestBody = provision.copy(
                                    space_guid = UUID.randomUUID().toString(),
                                    organization_guid = UUID.randomUUID().toString()
                            ),
                            expectedFinalStatusCodes = *intArrayOf(409),
                            expectedResponseBodyType = ResponseBodyType.NO_SCHEMA
                    )
                }
        )
    }

    fun validRetrievableInstanceContainer(
            instanceId: String,
            provision: ProvisionBody.ValidProvisioning,
            isRetrievable: Boolean
    ): DynamicTest {

        return DynamicTest.dynamicTest("Running valid GET for retrievable service instance") {
            val serviceInstance = provisionRequestRunner.getProvision(instanceId, 200)
            assertNotNull(serviceInstance, "Expected a valid service Instance Object.")
            assertTrue("When retrieving the instance the response did not match the expected value. \n" +
                    "service_id: expected ${provision.service_id} actual ${serviceInstance.serviceId} \n" +
                    "plan_id: expected ${provision.plan_id} actual ${serviceInstance.planId}")
            { serviceInstance.serviceId == provision.service_id && serviceInstance.planId == provision.plan_id }
        }
    }
}