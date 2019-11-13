package de.evoila.osb.checker.tests.containers

import de.evoila.osb.checker.tests.objects.Messages.DELETE_RESULT_MESSAGE
import de.evoila.osb.checker.tests.objects.Messages.VALID_BINDING_DISPLAY_NAME
import de.evoila.osb.checker.tests.objects.Messages.VALID_BINDING_MESSAGE
import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.request.BindingRequestRunner
import de.evoila.osb.checker.request.CatalogRequestRunner
import de.evoila.osb.checker.request.ResponseBodyType.NO_SCHEMA
import de.evoila.osb.checker.request.ResponseBodyType.VALID_BINDING
import de.evoila.osb.checker.request.bodies.BindingBody
import de.evoila.osb.checker.response.catalog.Plan
import de.evoila.osb.checker.response.operations.AsyncResponse
import de.evoila.osb.checker.response.operations.LastOperationResponse.State.GONE
import de.evoila.osb.checker.response.operations.LastOperationResponse.State.SUCCEEDED
import de.evoila.osb.checker.tests.objects.Messages.EXPECTED_FINAL_POLLING_STATE
import de.evoila.osb.checker.tests.objects.Messages.SKIPPING_BINDING_WITH_DIFFERENT_ATTRIBUTES
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicTest
import org.springframework.stereotype.Service
import kotlin.test.assertTrue

@Service
class BindingContainerService(
        val bindingRequestRunner: BindingRequestRunner,
        val catalogRequestRunner: CatalogRequestRunner,
        val configuration: Configuration
) {

    fun validBindingContainer(
            binding: BindingBody,
            instanceId: String,
            bindingId: String,
            isRetrievable: Boolean,
            service: de.evoila.osb.checker.response.catalog.Service,
            plan: Plan
    ): DynamicContainer {
        val bindingTests = createValidBindingTests(bindingId, binding, instanceId, plan)

        return DynamicContainer.dynamicContainer(VALID_BINDING_MESSAGE, if (isRetrievable) {
            bindingTests.plus(listOf(validRetrievableBindingContainer(instanceId, bindingId),
                    validDeleteTest(binding, instanceId, bindingId, service, plan)))
        } else {
            bindingTests.plus(validDeleteTest(binding, instanceId, bindingId, service, plan))
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
                        val bindingResponse = response.jsonPath().getObject("", AsyncResponse::class.java)
                        val state = bindingRequestRunner.putPolling(
                                instanceId = instanceId,
                                bindingId = bindingId,
                                operationData = bindingResponse.operation,
                                maxPollingDuration = plan.maximumPollingDuration,
                                requestBody = binding
                        )
                        assertTrue("$EXPECTED_FINAL_POLLING_STATE$state")
                        { SUCCEEDED == state }
                    }
                },
                DynamicTest.dynamicTest("Running PUT binding with same attribute again. Expecting StatusCode 200.") {
                    bindingRequestRunner.runPutBindingRequestAsync(
                            requestBody = binding,
                            instanceId = instanceId,
                            bindingId = bindingId,
                            expectedStatusCodes = *intArrayOf(200),
                            expectedResponseBody = NO_SCHEMA
                    )
                },
                if (catalogHasMultiplePlans()) {
                    DynamicTest.dynamicTest("Running PUT binding with different service or plan id, but same instance and binding id again. Expecting StatusCode 409.") {
                        bindingRequestRunner.runPutBindingRequestAsync(
                                requestBody = swapServiceAndPlanId(binding),
                                instanceId = instanceId,
                                bindingId = bindingId,
                                expectedStatusCodes = *intArrayOf(409),
                                expectedResponseBody = NO_SCHEMA
                        )
                    }
                } else {
                    DynamicTest.dynamicTest(SKIPPING_BINDING_WITH_DIFFERENT_ATTRIBUTES) {}
                }
        )
    }

    private fun catalogHasMultiplePlans(): Boolean = catalogRequestRunner.correctRequest().services.flatMap { it.plans }.size > 1

    private fun swapServiceAndPlanId(oldBindingBody: BindingBody): BindingBody {
        val catalog = catalogRequestRunner.correctRequest()
        return if (catalog.services.size > 1) {
            catalog.services.first { service -> service.id != oldBindingBody.serviceId }.let {
                oldBindingBody.copy(serviceId = it.id, planId = it.plans.first().id)
            }
        } else {
            oldBindingBody.copy(planId = catalog.services.first().plans.first { oldBindingBody.planId != it.id }.id)
        }
    }

    fun validDeleteTest(binding: BindingBody, instanceId: String, bindingId: String,
                        service: de.evoila.osb.checker.response.catalog.Service, plan: Plan): DynamicTest =
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
                        GONE == bindingRequestRunner.deletePolling(
                                instanceId = instanceId,
                                bindingId = bindingId,
                                operationData = asyncResponse.operation,
                                maxPollingDuration = plan.maximumPollingDuration,
                                serviceId = service.id,
                                planId = plan.id
                        )
                    }
                }
            }

    fun validRetrievableBindingContainer(instanceId: String, bindingId: String): DynamicTest {
        return DynamicTest.dynamicTest("Running GET for retrievable service binding" +
                " and expecting StatusCode: 200") {
            bindingRequestRunner.runGetBindingRequest(instanceId, bindingId, 200)
        }
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
}