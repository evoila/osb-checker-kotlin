package de.evoila.osb.checker.tests

import de.evoila.osb.checker.request.ResponseBodyType.*
import de.evoila.osb.checker.response.catalog.Plan
import de.evoila.osb.checker.response.catalog.Service
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.util.*

@DisplayName(value = "Binding Tests")
class BindingJUnit5 : BindingTestBase() {

    @TestFactory
    @DisplayName(value = "Valid Provision and Binding Tests.")
    fun validProvisionAndBindingTestRuns(): List<DynamicNode> {
        return configuration.initCustomCatalog(bindingContainers.catalog).services.flatMap { service ->
            service.plans.map { plan ->
                val instanceId = UUID.randomUUID().toString()
                val bindingId = UUID.randomUUID().toString()
                val dynamicContainers: MutableList<DynamicNode> = setUpValidProvisionRequestTest(service, plan, instanceId)
                val bindable = planIsBindable(service, plan)

                if (bindable) {
                    val binding = setUpValidBindingBody(service, plan)
                    dynamicContainers.add(dynamicContainer("Service ${service.name} Plan ${plan.name} is bindable. Testing binding operation with bindingId $bindingId", mutableListOf(
                            createSyncAndInvalidBindingTests(service, plan, instanceId, bindingId),
                            bindingContainers.validBindingContainer(
                                    binding = binding,
                                    instanceId = instanceId,
                                    bindingId = bindingId,
                                    isRetrievable = configuration.apiVersion > 2.13 && service.bindingsRetrievable ?: false,
                                    service = service,
                                    plan = plan
                            ))
                    ))
                } else {
                    dynamicContainers.add(dynamicTest("%SKIPPED%Service '${service.name}' Plan ${plan.name} is not bindable. Skipping binding tests.") {})
                }

                dynamicContainers.add(bindingContainers.validDeleteProvisionContainer(instanceId, service, plan))
                dynamicContainer(if (bindable) {
                    BINDABLE_MESSAGE
                } else {
                    PROVISION_DISPLAY_NAME
                }, dynamicContainers)
            }
        }
    }

    private fun createSyncAndInvalidBindingTests(
            service: Service,
            plan: Plan,
            instanceId: String,
            bindingId: String
    ): DynamicContainer {
        val bindingTests = mutableListOf<DynamicNode>()
        service.bindingsRetrievable?.let { bindingRetrievable ->
            if (configuration.apiVersion >= 2.14 && bindingRetrievable) {
                bindingTests.add(dynamicTest("should return status code 4XX when tying to fetch a non existing binding") {
                    bindingRequestRunner.runGetBindingRequest(instanceId, bindingId, *IntArray(100) { 400 + it })
                })
            }
        }
        if (configuration.apiVersion >= 2.14) {
            bindingTests.add(dynamicContainer("should handle sync requests correctly",
                    bindingContainers.createSyncBindingTest(
                            binding = setUpValidBindingBody(service, plan),
                            bindingId = bindingId,
                            instanceId = instanceId)
            ))
        }
        bindingTests.addAll(listOf(
                TestCase(
                        requestBody = setUpServiceIdMissingBindingBody(plan),
                        message = "should reject if missing service_id",
                        responseBodyType = VALID_BINDING,
                        statusCode = 400
                ),
                TestCase(
                        requestBody = setUpPlanIdMissingBindingBody(service, plan),
                        message = "should reject if missing plan_id",
                        responseBodyType = VALID_BINDING,
                        statusCode = 400
                )
        ).flatMap { testCase ->
            listOf(
                    dynamicTest("PUT ${testCase.message}") {
                        bindingRequestRunner.runPutBindingRequestAsync(
                                requestBody = testCase.requestBody,
                                instanceId = instanceId,
                                bindingId = bindingId,
                                expectedStatusCodes = *intArrayOf(testCase.statusCode),
                                expectedResponseBody = ERR
                        )
                    },
                    dynamicTest("DELETE ${testCase.message}") {
                        val bindingRequestBody = testCase.requestBody
                        bindingRequestRunner.runDeleteBindingRequestAsync(
                                serviceId = bindingRequestBody.serviceId,
                                planId = bindingRequestBody.planId,
                                instanceId = instanceId,
                                bindingId = bindingId,
                                expectedStatusCodes = *intArrayOf(410)
                        )
                    }
            )
        })
        return dynamicContainer("Run sync and invalid bindings attempts", bindingTests)
    }

    @TestFactory
    @DisplayName("PUT and DELETE and possibly GET bindings requests with non existing service instance Id. Expecting a 4XX Error Code ")
    fun testBindingOnNonExistingServiceInstance(): List<DynamicNode> {
        val invalidServiceInstanceId = UUID.randomUUID().toString()
        val bindingId = UUID.randomUUID().toString()

        return configuration.initCustomCatalog(catalogRequestRunner.correctRequest()).services.flatMap { service ->
            service.plans.filter { plan -> planIsBindable(service, plan) }.map { bindablePlan ->
                val binding = setUpValidBindingBody(service, bindablePlan)

                dynamicContainer("Testing service ${service.name} plan ${bindablePlan.name}", listOf(
                        dynamicTest("Testing PUT binding") {
                            bindingRequestRunner.runPutBindingRequestAsync(
                                    requestBody = binding,
                                    instanceId = invalidServiceInstanceId,
                                    bindingId = bindingId,
                                    expectedStatusCodes = *IntArray(100) { 400 + it },
                                    expectedResponseBody = NO_SCHEMA
                            )
                        },
                        dynamicTest("Testing DELETE binding") {
                            bindingRequestRunner.runDeleteBindingRequestAsync(
                                    serviceId = binding.serviceId,
                                    planId = binding.planId,
                                    instanceId = invalidServiceInstanceId,
                                    bindingId = bindingId,
                                    expectedStatusCodes = *IntArray(100) { 400 + it }
                            )
                        },
                        if (service.bindingsRetrievable == true) {
                            dynamicTest("Testing GET binding.") {
                                bindingRequestRunner.runGetBindingRequest(
                                        instanceId = invalidServiceInstanceId,
                                        bindingId = bindingId,
                                        expectedStatusCodes = *IntArray(100) { 400 + it }
                                )
                            }
                        } else dynamicTest("%SKIPPED%Binding is not retrievable. Skipping GET binding.") {}
                ))
            }
        }
    }

    companion object {
        private const val BINDABLE_MESSAGE = "Running a valid provision and run binding tests." +
                " Delete both afterwards. In case of a asynchronous service broker polling after each operation."
        private const val PROVISION_DISPLAY_NAME = "Running a valid provision and if necessary polling. Deleting it afterwards."
    }
}
