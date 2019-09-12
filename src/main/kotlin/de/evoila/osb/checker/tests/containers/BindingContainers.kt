package de.evoila.osb.checker.tests.containers

import de.evoila.osb.checker.request.BindingRequestRunner
import de.evoila.osb.checker.request.ProvisionRequestRunner
import de.evoila.osb.checker.request.ResponseBodyType.*
import de.evoila.osb.checker.request.bodies.BindingBody
import de.evoila.osb.checker.request.bodies.ProvisionBody
import de.evoila.osb.checker.response.operations.LastOperationResponse.State.*
import de.evoila.osb.checker.response.catalog.Plan
import de.evoila.osb.checker.response.operations.AsyncResponse
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicTest
import org.springframework.stereotype.Service
import java.util.*
import kotlin.test.assertTrue

@Service
class BindingContainers(
        val provisionRequestRunner: ProvisionRequestRunner,
        val bindingRequestRunner: BindingRequestRunner
) {

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
                                    GONE == provisionRequestRunner.polling(
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

    fun validBindingContainer(
            binding: BindingBody,
            instanceId: String,
            bindingId: String,
            isRetrievable: Boolean,
            plan: Plan
    ): DynamicContainer {

        return DynamicContainer.dynamicContainer(VALID_BINDING_MESSAGE, if (isRetrievable) {
            listOf(validRetrievableBindingContainer(instanceId, bindingId, 404)).plus(
                    createValidBindingTests(
                            bindingId = bindingId,
                            binding = binding,
                            instanceId = instanceId,
                            plan = plan
                    ).plus(listOf(validRetrievableBindingContainer(instanceId, bindingId, 200),
                            validDeleteTest(binding, instanceId, bindingId, plan))
                    )
            )
        } else {
            createValidBindingTests(bindingId, binding, instanceId, plan)
                    .plus(validDeleteTest(binding, instanceId, bindingId, plan))
        })
    }

    fun createValidBindingTests(
            bindingId: String,
            binding: BindingBody,
            instanceId: String,
            plan: Plan
    ): List<DynamicTest> {
        return listOf(
                DynamicTest.dynamicTest(VALID_BINDING_DISPLAY_NAME + bindingId) {
                    val response = bindingRequestRunner.runPutBindingRequestAsync(
                            requestBody = binding,
                            instanceId = instanceId,
                            bindingId = bindingId,
                            expectedStatusCodes = *intArrayOf(201, 202),
                            expectedResponseBody = VALID_BINDING
                    )

                    if (response.statusCode() == 202) {
                        val provision = response.jsonPath().getObject("", AsyncResponse::class.java)
                        val state = bindingRequestRunner.polling(
                                instanceId = instanceId,
                                bindingId = bindingId,
                                expectedFinalStatusCode = 200,
                                operationData = provision.operation,
                                maxPollingDuration = plan.maximumPollingDuration
                        )
                        assertTrue("Expected the final polling state to be \"succeeded\" but was $state")
                        { SUCCEEDED == state }
                    }
                },
                DynamicTest.dynamicTest("Running PUT binding with same attribute again. Expecting StatusCode 200.") {
                    val response = bindingRequestRunner.runPutBindingRequestAsync(
                            requestBody = binding,
                            instanceId = instanceId,
                            bindingId = bindingId,
                            expectedStatusCodes = *intArrayOf(200),
                            expectedResponseBody = NO_SCHEMA
                    )
                },
                DynamicTest.dynamicTest("Running PUT binding with different attribute again. Expecting StatusCode 409.") {
                    val response = bindingRequestRunner.runPutBindingRequestAsync(
                            requestBody = binding.copy(

                            ),
                            instanceId = instanceId,
                            bindingId = bindingId,
                            expectedStatusCodes = *intArrayOf(409),
                            expectedResponseBody = NO_SCHEMA
                    )
                }
        )
    }

    fun validDeleteTest(binding: BindingBody, instanceId: String, bindingId: String, plan: Plan): DynamicTest =
            DynamicTest.dynamicTest("Deleting binding with bindingId $bindingId") {
                val response = bindingRequestRunner.runDeleteBindingRequestAsync(
                        serviceId = binding.serviceId,
                        planId = binding.planId,
                        instanceId = instanceId,
                        bindingId = bindingId,
                        expectedStatusCodes = *intArrayOf(200, 202)
                )

                if (response.statusCode() == 202) {
                    val asyncResponse = response.jsonPath().getObject("", AsyncResponse::class.java)
                    assertTrue(DELETE_RESULT_MESSAGE) {
                        GONE == bindingRequestRunner.polling(
                                instanceId = instanceId,
                                bindingId = bindingId,
                                expectedFinalStatusCode = 410,
                                operationData = asyncResponse.operation,
                                maxPollingDuration = plan.maximumPollingDuration
                        )
                    }
                }
            }

    fun validRetrievableBindingContainer(instanceId: String, bindingId: String, expectedStatusCode: Int): DynamicTest {
        return DynamicTest.dynamicTest("Running GET for retrievable service binding" +
                " and expecting StatusCode: $expectedStatusCode") {
            bindingRequestRunner.runGetBindingRequest(expectedStatusCode, instanceId, bindingId)
        }
    }

    fun validRetrievableInstanceContainer(
            instanceId: String,
            provision: ProvisionBody.ValidProvisioning,
            isRetrievable: Boolean
    ): DynamicTest {

        return DynamicTest.dynamicTest("Running valid GET for retrievable service instance") {
            val serviceInstance = provisionRequestRunner.getProvision(instanceId, isRetrievable)
            assertTrue("When retrieving the instance the response did not match the expected value. \n" +
                    "service_id: expected ${provision.service_id} actual ${serviceInstance.serviceId} \n" +
                    "plan_id: expected ${provision.plan_id} actual ${serviceInstance.planId}")
            { serviceInstance.serviceId == provision.service_id && serviceInstance.planId == provision.plan_id }
        }
    }

    private fun createValidProvisionTests(
            instanceId: String,
            provision: ProvisionBody.ValidProvisioning,
            plan: Plan
    ): List<DynamicTest> {

        return listOf(
                DynamicTest.dynamicTest("Running valid PUT provision with instanceId $instanceId" +
                        " for service ${provision.service_id}" +
                        " and plan id: ${provision.plan_id}") {
                    val response = provisionRequestRunner.runPutProvisionRequestAsync(
                            instanceId = instanceId,
                            requestBody = provision,
                            expectedFinalStatusCodes = *intArrayOf(201, 202, 200),
                            expectedResponseBodyType = VALID_PROVISION
                    )

                    if (response.statusCode() == 202) {
                        val provisionResponse = response.jsonPath().getObject("", AsyncResponse::class.java)
                        val state = provisionRequestRunner.polling(
                                instanceId = instanceId,
                                expectedFinalStatusCode = 200,
                                operationData = provisionResponse.operation,
                                maxPollingDuration = plan.maximumPollingDuration
                        )
                        assertTrue(EXPECTED_FINAL_POLLING_STATE + state)
                        { SUCCEEDED == state }
                    }
                },
                DynamicTest.dynamicTest("Running valid PUT provision with same attributes again. Expecting Status 200.") {
                    provisionRequestRunner.runPutProvisionRequestAsync(
                            instanceId = instanceId,
                            requestBody = provision,
                            expectedFinalStatusCodes = *intArrayOf(200),
                            expectedResponseBodyType = VALID_PROVISION
                    )
                },
                DynamicTest.dynamicTest("Running valid PUT provision with different attributes again. Expecting Status 409.") {
                    provisionRequestRunner.runPutProvisionRequestAsync(
                            instanceId = instanceId,
                            requestBody = provision.copy(
                                    space_guid = UUID.randomUUID().toString(),
                                    organization_guid = UUID.randomUUID().toString()
                            ),
                            expectedFinalStatusCodes = *intArrayOf(409),
                            expectedResponseBodyType = NO_SCHEMA
                    )
                }
        )
    }

    fun validProvisionContainer(
            instanceId: String,
            plan: Plan,
            provision: ProvisionBody.ValidProvisioning,
            isRetrievable: Boolean
    ): DynamicContainer {
        val provisionTests = createValidProvisionTests(instanceId, provision, plan)
        var displayName = VALID_PROVISION_DISPLAY_NAME

        displayName += if (isRetrievable) {
            provisionTests.plus(validRetrievableInstanceContainer(instanceId, provision, isRetrievable))
            VALID_FETCH_PROVISION
        } else {
            ""
        }

        return DynamicContainer.dynamicContainer("$displayName.", provisionTests)
    }

    fun createSyncBindingTest(
            binding: BindingBody,
            instanceId: String,
            bindingId: String
    ): List<DynamicTest> = listOf(
            DynamicTest.dynamicTest("Sync PUT binding request") {
                bindingRequestRunner.runPutBindingRequestSync(binding, instanceId, bindingId)
            },
            DynamicTest.dynamicTest("Sync DELETE binding request") {
                bindingRequestRunner.runDeleteBindingRequestSync(
                        instanceId = instanceId,
                        bindingId = bindingId,
                        serviceId = binding.serviceId,
                        planId = binding.planId
                )
            }
    )

    companion object {
        private const val VALID_PROVISION_DISPLAY_NAME = "Creating Service Instance"
        private const val VALID_BINDING_DISPLAY_NAME = "Running valid PUT binding with bindingId "
        private const val VALID_BINDING_MESSAGE = "Running PUT binding and DELETE binding afterwards"
        private const val DELETE_PROVISION_MESSAGE = "DELETE provision and if the service broker is async polling afterwards"
        private const val DELETE_RESULT_MESSAGE = "Delete has to result in 410"
        private const val VALID_FETCH_PROVISION = ", fetching it"
        private const val EXPECTED_FINAL_POLLING_STATE = "Expected the final polling state to be \"succeeded\" but was "
    }
}