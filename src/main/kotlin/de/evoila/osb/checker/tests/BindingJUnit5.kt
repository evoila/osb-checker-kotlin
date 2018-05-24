package de.evoila.osb.checker.tests

import de.evoila.osb.checker.request.BindingRequestRunner
import de.evoila.osb.checker.request.bodies.ProvisionBody
import de.evoila.osb.checker.request.bodies.RequestBody
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import java.util.stream.Stream
import kotlin.test.assertTrue

class BindingJUnit5 : TestBase() {

  @Autowired
  lateinit var bindingRequestRunner: BindingRequestRunner

  @TestFactory
  fun runValidBindings(): Stream<DynamicNode> {
    wire()
    val catalog = catalogRequestRunner.correctRequest()

    val dynamicNodes = mutableListOf<DynamicNode>()

    catalog.services.forEach { service ->
      service.plans.forEach { plan ->

        val instanceId = UUID.randomUUID().toString()
        val bindingId = UUID.randomUUID().toString()

        val provision = ProvisionBody.ValidProvisioning(service, plan)
        val binding = RequestBody.ValidBinding(service.id, plan.id)

        dynamicNodes.add(
            dynamicContainer("Running Valid Provision with InstanceId $instanceId and Valid Binding with BindingId $bindingId", listOf(

                dynamicContainer("Provision and Polling for later Binding", listOf(
                    dynamicTest("Running Valid Provision with InstanceId $instanceId") {
                      provisionRequestRunner.runPutProvisionRequestAsync(instanceId, provision, 202)
                    },
                    dynamicTest("RunningPolling") {
                      assert(provisionRequestRunner.waitForFinish(instanceId, 200) == "succeeded")
                    }
                )),
                dynamicContainer("Running PUT Binding and DELETE Binding afterwards", listOf(
                    dynamicTest("PUT Binding") {
                      bindingRequestRunner.runPutBindingRequest(binding, 201, instanceId, bindingId)
                    },
                    dynamicTest("DELETE Binding") {
                      bindingRequestRunner.runDeleteBindingRequest(binding.service_id, binding.plan_id, 200, instanceId, bindingId)
                    }
                )),
                dynamicContainer("Deleting Provision and Polling afterwards",
                    listOf(
                        dynamicTest("deleting Provision") {
                          val statusCode = provisionRequestRunner.runDeleteProvisionRequestAsync(instanceId, service.id, plan.id)
                          assertTrue("statusCode should be 200 or 202 but was $statusCode.") { statusCode in listOf(200, 202) }
                        },
                        dynamicTest("polling Provision") {
                          provisionRequestRunner.waitForFinish(instanceId, 410)
                        }
                    ))
            ))
        )
      }
    }
    return dynamicNodes.parallelStream()
  }

  @TestFactory
  fun runInvalidBindingAttempts(): List<DynamicNode> {
    val catalog = setupCatalog()
    val service = catalog.services.first()
    val plan = service.plans.first()
    val provision = ProvisionBody.ValidProvisioning(catalog.services.first(), plan)
    val instanceId = UUID.randomUUID().toString()
    val bindingId = UUID.randomUUID().toString()

    val dynamicNodes = mutableListOf<DynamicNode>()

    dynamicNodes.add(
        dynamicContainer("Provision and Polling for later Binding", listOf(
            dynamicTest("Running Valid Provision with InstanceId $instanceId") {
              provisionRequestRunner.runPutProvisionRequestAsync(instanceId, provision, 202)
            },
            dynamicTest("RunningPolling") {
              assert(provisionRequestRunner.waitForFinish(instanceId, 200) == "succeeded")
            }
        ))
    )

    listOf(
        TestCase(
            requestBody = RequestBody.ValidBinding(
                service_id = null,
                plan_id = plan.id
            ),
            message = "should reject if missing service_id"
        ),
        TestCase(
            requestBody = RequestBody.ValidBinding(
                service_id = service.id,
                plan_id = null
            ),
            message = "should reject if missing plan_id"
        )
    ).forEach {
      dynamicNodes.add(
          dynamicTest("PUT ${it.message}")
          { bindingRequestRunner.runPutBindingRequest(it.requestBody, 400, instanceId, bindingId) }
      )
      dynamicNodes.add(
          dynamicTest("DELETE ${it.message}")
          {
            val bindingRequestBody = it.requestBody as RequestBody.ValidBinding

            bindingRequestRunner.runDeleteBindingRequest(
                serviceId = bindingRequestBody.service_id,
                planId = bindingRequestBody.plan_id,
                expectedStatusCode = 400,
                instanceId = instanceId,
                bindingId = bindingId)
          }
      )
    }
    dynamicNodes.add(
        dynamicContainer("Deleting Provision and Polling afterwards",
            listOf(
                dynamicTest("deleting Provision") {
                  val statusCode = provisionRequestRunner.runDeleteProvisionRequestAsync(instanceId, service.id, plan.id)
                  assertTrue("statusCode should be 200 or 202 but was $statusCode.") { statusCode in listOf(200, 202) }
                },
                dynamicTest("polling Provision") {
                  provisionRequestRunner.waitForFinish(instanceId, 410)
                }
            ))
    )

    return dynamicNodes
  }
}
