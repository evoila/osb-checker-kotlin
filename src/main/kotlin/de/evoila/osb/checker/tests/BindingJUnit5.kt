package de.evoila.osb.checker.tests

import de.evoila.osb.checker.request.BindingRequestRunner
import de.evoila.osb.checker.request.ResponseBodyType.ERR
import de.evoila.osb.checker.request.ResponseBodyType.VALID_BINDING
import de.evoila.osb.checker.request.bodies.BindingBody
import de.evoila.osb.checker.request.bodies.ProvisionBody
import de.evoila.osb.checker.response.catalog.Catalog
import de.evoila.osb.checker.response.catalog.Plan
import de.evoila.osb.checker.response.catalog.Service
import de.evoila.osb.checker.tests.containers.BindingContainers
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class BindingJUnit5 : TestBase() {

    @Autowired
    lateinit var bindingRequestRunner: BindingRequestRunner
    @Autowired
    lateinit var bindingContainerFactory: BindingContainers

    @TestFactory
    fun runValidBindings(): List<DynamicNode> {
        val catalog = configuration.initCustomCatalog() ?: catalogRequestRunner.correctRequest()
        val dynamicNodes = mutableListOf<DynamicNode>()

        catalog.services.forEach { service ->
            service.plans.forEach { plan ->
                val instanceId = UUID.randomUUID().toString()
                val bindingId = UUID.randomUUID().toString()
                val needsAppGuid = needAppGUID(plan)
                val provision = if (configuration.apiVersion == 2.15 && plan.maintenanceInfo != null)
                    ProvisionBody.ValidProvisioning(service, plan, plan.maintenanceInfo)
                else ProvisionBody.ValidProvisioning(service, plan)

                val binding = if (needsAppGuid) BindingBody.ValidBindingWithAppGuid(service.id, plan.id)
                else BindingBody.ValidBinding(service.id, plan.id)

                configuration.provisionParameters.let {
                    if (it.containsKey(plan.id)) {
                        provision.parameters = it[plan.id]
                    }
                }
                configuration.bindingParameters.let {
                    if (it.containsKey(plan.id)) {
                        binding.parameters = it[plan.id]
                    }
                }
                val testContainers = mutableListOf(
                        bindingContainerFactory.validProvisionContainer(
                                instanceId = instanceId,
                                plan = plan,
                                provision = provision,
                                isRetrievable = configuration.apiVersion > 2.13 && (service.instancesRetrievable
                                        ?: false))
                )
                testContainers.add(bindingContainerFactory.validBindingContainer(
                        binding = binding,
                        instanceId = instanceId,
                        bindingId = bindingId,
                        isRetrievable = configuration.apiVersion > 2.13 && service.bindingsRetrievable ?: false,
                        plan = plan))
                testContainers.add(bindingContainerFactory.validDeleteProvisionContainer(instanceId, service, plan))
                dynamicNodes.add(dynamicContainer(VALID_BINDING_MESSAGE, testContainers))
            }
        }

        return dynamicNodes
    }

    @TestFactory
    fun runSyncAndInvalidBindingAttempts(): List<DynamicNode> {
        val catalog = configuration.initCustomCatalog() ?: catalogRequestRunner.correctRequest()
        val service = findBindableService(catalog) ?: return emptyList()
        val plan = findBindablePlan(service)
        val needsAppGuid: Boolean = needAppGUID(plan)
        val provision = if (configuration.apiVersion == 2.15 && plan.maintenanceInfo != null) {
            ProvisionBody.ValidProvisioning(service, plan, plan.maintenanceInfo)
        } else {
            ProvisionBody.ValidProvisioning(service, plan)
        }
        val instanceId = UUID.randomUUID().toString()
        val bindingId = UUID.randomUUID().toString()
        val dynamicNodes = mutableListOf<DynamicNode>()
        dynamicNodes.add(
                bindingContainerFactory.validProvisionContainer(
                        instanceId = instanceId,
                        plan = plan,
                        provision = provision, isRetrievable = configuration.apiVersion > 2.13 && (service.instancesRetrievable
                        ?: false)
                )
        )
        val bindingTests = mutableListOf<DynamicNode>(
                dynamicContainer("should handle sync requests correctly",
                        bindingContainerFactory.createSyncBindingTest(
                                binding = if (needsAppGuid) BindingBody.ValidBindingWithAppGuid(service.id, plan.id)
                                else BindingBody.ValidBinding(service.id, plan.id),
                                bindingId = bindingId,
                                instanceId = instanceId
                        )
                ))
        listOf(
                TestCase(
                        requestBody =
                        if (needsAppGuid) BindingBody.ValidBindingWithAppGuid(null, plan.id)
                        else BindingBody.ValidBinding(null, plan.id),
                        message = "should reject if missing service_id",
                        responseBodyType = VALID_BINDING
                ),
                TestCase(
                        requestBody = if (needsAppGuid) BindingBody.ValidBindingWithAppGuid(service.id, null)
                        else BindingBody.ValidBinding(service.id, null),
                        message = "should reject if missing plan_id",
                        responseBodyType = VALID_BINDING
                )
        ).forEach {
            bindingTests.add(
                    dynamicTest("PUT ${it.message}") {
                        bindingRequestRunner.runPutBindingRequestAsync(
                                requestBody = it.requestBody,
                                instanceId = instanceId,
                                bindingId = bindingId,
                                expectedStatusCodes = *intArrayOf(400),
                                expectedResponseBody = ERR
                        )
                    }
            )
            bindingTests.add(
                    dynamicTest("DELETE ${it.message}") {
                        val bindingRequestBody = it.requestBody
                        bindingRequestRunner.runDeleteBindingRequestAsync(
                                serviceId = bindingRequestBody.service_id,
                                planId = bindingRequestBody.plan_id,
                                instanceId = instanceId,
                                bindingId = bindingId,
                                expectedStatusCodes = *intArrayOf(410)
                        )
                    }
            )
        }
        dynamicNodes.add(dynamicContainer("Create a Service Instance and run sync and invalid bindings attempts", bindingTests))
        dynamicNodes.add(bindingContainerFactory.validDeleteProvisionContainer(instanceId, service, plan))

        return dynamicNodes
    }

    private fun findBindableService(catalog: Catalog): Service? =
            catalog.services.firstOrNull { service -> service.bindable }

    private fun findBindablePlan(service: Service): Plan =
            service.plans.first { plan -> plan.bindable ?: true }

    fun needAppGUID(plan: Plan): Boolean = plan.metadata?.customParameters?.usesServicesKeys
            ?: configuration.usingAppGuid

    companion object {
        private const val VALID_BINDING_MESSAGE = "Running a valid provision and if the service is bindable a valid" +
                " binding. Deleting both afterwards. In case of a async service broker poll afterwards."
    }
}
