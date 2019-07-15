package de.evoila.osb.checker.tests

import de.evoila.osb.checker.request.ProvisionRequestRunner
import de.evoila.osb.checker.request.bodies.ProvisionBody
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import kotlin.test.assertTrue

class ProvisionJUnit5 : TestBase() {

  @Autowired
  lateinit var provisionRequestRunner: ProvisionRequestRunner

  @TestFactory
  fun runSyncTest(): List<DynamicNode> {

    val catalog = configuration.initCustomCatalog() ?: catalogRequestRunner.correctRequest()
    val instanceId = UUID.randomUUID().toString()
    val service = catalog.services.first()
    val plan = service.plans.first()
    val provisionRequestBody = ProvisionBody.ValidProvisioning(service, plan)
    val dynamicNodes = mutableListOf<DynamicNode>()

    dynamicNodes.add(
        dynamicContainer("should handle sync requests correctly", listOf(
            dynamicTest("Sync PUT request") {
              val statusCodePut = provisionRequestRunner.runPutProvisionRequestSync(instanceId, provisionRequestBody)
              assertTrue("Should return  201 in case of a sync service broker or 422 if it's async but it was $statusCodePut.")
              { statusCodePut in listOf(201, 422) }
            },
            dynamicTest("Sync DELETE request") {
              val statusCodePut = provisionRequestRunner.runDeleteProvisionRequestSync(instanceId, provisionRequestBody.service_id, provisionRequestBody.plan_id)
              assertTrue("Should return  200 in case of a sync Service Broker or 422 if it's async but it was $statusCodePut.")
              { statusCodePut in listOf(200, 422) }
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
                service_id = null,
                plan_id = plan.id
            ),
            message = "should reject if missing service_id"
        ),
        TestCase(
            requestBody = ProvisionBody.ValidProvisioning(
                service_id = service.id,
                plan_id = null
            ),
            message = "should reject if missing plan_id"
        ),
        TestCase(
            requestBody = ProvisionBody.NoServiceFieldProvisioning(
                service
            ),
            message = "should reject if missing service_id field"
        ),
        TestCase(
            requestBody = ProvisionBody.NoPlanFieldProvisioning(
                plan
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
                "Invalid", plan.id
            ),
            message = "should reject if missing service_id is Invalid"
        ),
        TestCase(
            requestBody = ProvisionBody.ValidProvisioning(
                service.id, "Invalid"
            ),
            message = "should reject if missing plan_id is Invalid"
        )
    ).forEach {
      dynamicNodes.add(
          dynamicTest("PUT ${it.message}") {
            val statusCode = provisionRequestRunner.runPutProvisionRequestAsync(instanceId, it.requestBody)
            assertTrue("Expected status code is 400 but was $statusCode") {
              400 == statusCode
            }
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
                service_id = null,
                plan_id = plan.id
            )
        ),
        TestCase(
            message = "should reject if plan_id is missing",
            requestBody = ProvisionBody.ValidProvisioning(
                service_id = service.id,
                plan_id = null
            )
        )
    ).forEach {
      dynamicNodes.add(dynamicTest("DELETE ${it.message}")
      {
        val provisionBody = it.requestBody

        val statusCode = provisionRequestRunner.runDeleteProvisionRequestAsync(
            serviceId = provisionBody.service_id,
            planId = provisionBody.plan_id,
            instanceId = instanceId
        )
        assertTrue("Should decline a invalid DELETE request with 400 but was $statusCode") { statusCode == 400 }
      }
      )
    }

    return dynamicNodes
  }
}