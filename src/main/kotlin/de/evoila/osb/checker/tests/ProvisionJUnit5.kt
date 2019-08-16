package de.evoila.osb.checker.tests

import de.evoila.osb.checker.request.ProvisionRequestRunner
import de.evoila.osb.checker.request.bodies.ProvisionBody
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
      ProvisionBody.ValidProvisioning(service, plan, plan.maintenanceInfo)
    } else {
      ProvisionBody.ValidProvisioning(service, plan)
    }
    val dynamicNodes = mutableListOf<DynamicNode>()
    dynamicNodes.add(
        dynamicContainer("should handle sync requests correctly", listOf(
            dynamicTest("Sync PUT request") {
              provisionRequestRunner.runPutProvisionRequestSync(instanceId, provisionRequestBody)
            },
            dynamicTest("Sync DELETE request") {
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
            requestBody = ProvisionBody.ValidProvisioning(
                service_id = "",
                plan_id = plan.id,
                maintenance_info = if (configuration.apiVersion >= 2.15) {
                  plan.maintenanceInfo
                } else {
                  null
                }
            ),
            message = "should reject if missing service_id"
        ),
        TestCase(
            requestBody = ProvisionBody.ValidProvisioning(
                service_id = service.id,
                plan_id = "",
                maintenance_info = if (configuration.apiVersion >= 2.15) {
                  plan.maintenanceInfo
                } else {
                  null
                }
            ),
            message = "should reject if missing plan_id"
        ),
        TestCase(
            requestBody = ProvisionBody.NoServiceFieldProvisioning(
                plan
            ),
            message = "should reject if missing service_id field"
        ),
        TestCase(
            requestBody = ProvisionBody.NoPlanFieldProvisioning(
                service
            ),
            message = "should reject if missing plan_id field"
        ),
        TestCase(
            requestBody = ProvisionBody.NoSpaceFieldProvisioning(
                service, plan
            ),
            message = "should reject if missing service_id field"
        ),
        TestCase(
            requestBody = ProvisionBody.NoOrgFieldProvisioning(
                service, plan
            ),
            message = "should reject if missing service_id field"
        ),
        TestCase(
            requestBody = ProvisionBody.ValidProvisioning(
                "Invalid", plan.id,
                maintenance_info = if (configuration.apiVersion == 2.15) {
                  plan.maintenanceInfo
                } else {
                  null
                }
            ),
            message = "should reject if missing service_id is Invalid"
        ),
        TestCase(
            requestBody = ProvisionBody.ValidProvisioning(
                service.id, "Invalid",
                maintenance_info = if (configuration.apiVersion >= 2.15) {
                  plan.maintenanceInfo
                } else {
                  null
                }
            ),
            message = "should reject if missing plan_id is Invalid"
        )
    ).forEach {
      dynamicNodes.add(
          dynamicTest("PUT ${it.message}") {
            provisionRequestRunner.runPutProvisionRequestAsync(instanceId, it.requestBody, 400)
          }
      )
    }

    if (configuration.apiVersion >= 2.15) {
      ProvisionBody.ValidProvisioning(service, plan, MaintenanceInfo("Invalid", "Should return 422"))
      dynamicNodes.add(
          dynamicTest("PUT should reject if maintenance_info doesn't match") {
            provisionRequestRunner.runPutProvisionRequestAsync(instanceId,
                ProvisionBody.ValidProvisioning(service, plan, MaintenanceInfo("Invalid", "Should return 422")), 422)
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
            requestBody = ProvisionBody.ValidProvisioning(
                service_id = "",
                plan_id = plan.id
            )
        ),
        TestCase(
            message = "should reject if plan_id is missing",
            requestBody = ProvisionBody.ValidProvisioning(
                service_id = service.id,
                plan_id = ""
            )
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