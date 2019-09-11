package de.evoila.osb.checker.tests

import de.evoila.osb.checker.request.ProvisionRequestRunner
import de.evoila.osb.checker.request.ResponseBodyType.*
import de.evoila.osb.checker.request.bodies.ProvisionBody
import de.evoila.osb.checker.request.bodies.ProvisionBody.ValidProvisioning
import de.evoila.osb.checker.response.catalog.MaintenanceInfo
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class ProvisionJUnit5 : TestBase() {

    @Autowired
    lateinit var provisionRequestRunner: ProvisionRequestRunner

    @TestFactory
    fun runSyncTest(): List<DynamicNode> {
        val catalog = configuration.initCustomCatalog() ?: catalogRequestRunner.correctRequest()
        val instanceId = UUID.randomUUID().toString()
        val service = catalog.services.first()
        val plan = service.plans.first()
        val provisionRequestBody = if (configuration.apiVersion >= 2.15 && plan.maintenanceInfo != null) {
            ValidProvisioning(service, plan, plan.maintenanceInfo)
        } else {
            ValidProvisioning(service, plan)
        }
        val dynamicNodes = mutableListOf<DynamicNode>()
        dynamicNodes.add(
                dynamicContainer("should handle sync requests correctly", listOf(
                        dynamicTest("Sync PUT provision request") {
                            provisionRequestRunner.runPutProvisionRequestSync(instanceId, provisionRequestBody)
                        },
                        dynamicTest("Sync DELETE provision request") {
                            provisionRequestRunner.runDeleteProvisionRequestSync(
                                    instanceId = instanceId,
                                    serviceId = provisionRequestBody.service_id,
                                    planId = provisionRequestBody.plan_id)
                        }
                ))
        )

        return dynamicNodes
    }

    @TestFactory
    fun runInvalidAsyncPutTest(): List<DynamicNode> {
        val catalog = configuration.initCustomCatalog() ?: catalogRequestRunner.correctRequest()
        val service = catalog.services.first()
        val plan = service.plans.first()
        val instanceId = UUID.randomUUID().toString()
        val dynamicNodes = mutableListOf<DynamicNode>()
        listOf(
                TestCase(
                        requestBody = ValidProvisioning(
                                service_id = "",
                                plan_id = plan.id,
                                maintenance_info = if (configuration.apiVersion >= 2.15) {
                                    plan.maintenanceInfo
                                } else {
                                    null
                                }
                        ),
                        message = "should reject if missing service_id",
                        responseBodyType = ERR
                ),
                TestCase(
                        requestBody = ValidProvisioning(
                                service_id = service.id,
                                plan_id = "",
                                maintenance_info = if (configuration.apiVersion >= 2.15) {
                                    plan.maintenanceInfo
                                } else {
                                    null
                                }
                        ),
                        message = "should reject if missing plan_id",
                        responseBodyType = ERR
                ),
                TestCase(
                        requestBody = ProvisionBody.NoServiceFieldProvisioning(plan),
                        message = "should reject if missing service_id field",
                        responseBodyType = ERR
                ),
                TestCase(
                        requestBody = ProvisionBody.NoPlanFieldProvisioning(service),
                        message = "should reject if missing plan_id field",
                        responseBodyType = ERR
                ),
                TestCase(
                        requestBody = ProvisionBody.NoSpaceFieldProvisioning(service, plan),
                        message = "should reject if missing service_id field",
                        responseBodyType = ERR
                ),
                TestCase(requestBody = ProvisionBody.NoOrgFieldProvisioning(service, plan),
                        message = "should reject if missing service_id field",
                        responseBodyType = ERR
                ),
                TestCase(
                        requestBody = ValidProvisioning(
                                "Invalid", plan.id,
                                maintenance_info = if (configuration.apiVersion == 2.15) {
                                    plan.maintenanceInfo
                                } else {
                                    null
                                }
                        ),
                        message = "should reject if missing service_id is Invalid",
                        responseBodyType = ERR
                ),
                TestCase(
                        requestBody = ValidProvisioning(
                                service.id, "Invalid",
                                maintenance_info = if (configuration.apiVersion >= 2.15) {
                                    plan.maintenanceInfo
                                } else {
                                    null
                                }
                        ),
                        message = "should reject if missing plan_id is Invalid",
                        responseBodyType = ERR
                )
        ).forEach {
            dynamicNodes.add(
                    dynamicTest("PUT ${it.message}") {
                        provisionRequestRunner.runPutProvisionRequestAsync(instanceId,
                                requestBody = it.requestBody,
                                expectedFinalStatusCodes = *intArrayOf(400),
                                expectedResponseBodyType = it.responseBodyType
                        )
                    }
            )
        }

        if (configuration.apiVersion >= 2.15) {
            ValidProvisioning(service, plan, MaintenanceInfo("Invalid", "Should return 422"))
            dynamicNodes.add(
                    dynamicTest("PUT should reject if maintenance_info doesn't match") {
                        provisionRequestRunner.runPutProvisionRequestAsync(instanceId,
                                requestBody = ValidProvisioning(service,
                                        plan,
                                        MaintenanceInfo("Invalid", "Should return 422")
                                ),
                                expectedFinalStatusCodes = *intArrayOf(422),
                                expectedResponseBodyType = ERR_MAINTENANCE_INFO
                        )
                    }
            )
        }

        return dynamicNodes
    }

    @TestFactory
    fun runInvalidAsyncDeleteTest(): List<DynamicNode> {
        val catalog = configuration.initCustomCatalog() ?: catalogRequestRunner.correctRequest()
        val service = catalog.services.first()
        val plan = service.plans.first()
        val instanceId = UUID.randomUUID().toString()
        val dynamicNodes = mutableListOf<DynamicNode>()
        listOf(
                TestCase(
                        message = "should reject if service_id is missing",
                        requestBody = ValidProvisioning("", plan.id),
                        responseBodyType = ERR
                ),
                TestCase(
                        message = "should reject if plan_id is missing",
                        requestBody = ValidProvisioning(service.id, ""),
                        responseBodyType = ERR
                )
        ).forEach {
            dynamicNodes.add(dynamicTest("DELETE ${it.message}") {
                val provisionBody = it.requestBody
                provisionRequestRunner.runDeleteProvisionRequestAsync(
                        serviceId = nullIfNotSet(provisionBody.service_id),
                        planId = nullIfNotSet(provisionBody.plan_id),
                        instanceId = instanceId,
                        expectedFinalStatusCodes = intArrayOf(400)
                )
            }
            )
        }

        return dynamicNodes
    }

    private fun nullIfNotSet(value: String): String? {
        return if (value.isNotEmpty()) {
            value
        } else {
            null
        }
    }
}