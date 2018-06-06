package de.evoila.osb.checker.tests

import de.evoila.osb.checker.request.BindingRequestRunner
import de.evoila.osb.checker.request.bodies.BindingBody
import de.evoila.osb.checker.request.bodies.ProvisionBody
import de.evoila.osb.checker.response.Plan
import de.evoila.osb.checker.response.Service
import org.junit.jupiter.api.DynamicContainer
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
    val catalog = catalogRequestRunner.correctRequest()
    val dynamicNodes = mutableListOf<DynamicNode>()

    catalog.services.forEach { service ->
      service.plans.forEach { plan ->

        val instanceId = UUID.randomUUID().toString()
        val bindingId = UUID.randomUUID().toString()
        val needsAppGuid = !plan.metadata.customParameters.usesServicesKeys

        val provision = ProvisionBody.ValidProvisioning(service, plan)
        val binding = if (configuration.usingAppGuid) BindingBody.ValidBindingWithAppGuid(service.id, plan.id) else BindingBody.ValidBinding(service.id, plan.id)

        configuration.parameters.let {
          if (it.containsKey(plan.id)) {
            provision.parameters = it[plan.id]
          }
        }

        val testContainers = mutableListOf(validProvisionContainer(instanceId, provision))

        val bindable = plan.bindable ?: service.bindable

        if (bindable) {
          testContainers.add(validBindingContainer(binding, instanceId, bindingId))
        }

        testContainers.add(validDeleteProvisionContainer(instanceId, service, plan))

        dynamicNodes.add(
            dynamicContainer("Running a valid provision with instanceId $instanceId and if it is bindable a valid binding with bindingId $bindingId", testContainers)
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
    val bindable = plan.bindable ?: service.bindable
    val needsAppGuid = !plan.metadata.customParameters.usesServicesKeys

    if (!bindable) {
      return emptyList()
    }

    val provision = ProvisionBody.ValidProvisioning(catalog.services.first(), plan)
    val instanceId = UUID.randomUUID().toString()
    val bindingId = UUID.randomUUID().toString()

    val dynamicNodes = mutableListOf<DynamicNode>()

    dynamicNodes.add(
        validProvisionContainer(instanceId, provision)
    )

    listOf(
        TestCase(
            requestBody =
            if (configuration.usingAppGuid) BindingBody.ValidBindingWithAppGuid(null, plan.id) else BindingBody.ValidBinding(null, plan.id),
            message = "should reject if missing service_id"
        ),
        TestCase(
            requestBody = if (configuration.usingAppGuid) BindingBody.ValidBindingWithAppGuid(service.id, null) else BindingBody.ValidBinding(service.id, null),
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
            val bindingRequestBody = it.requestBody

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
        validDeleteProvisionContainer(instanceId, service, plan)
    )
    return dynamicNodes
  }

  private fun validDeleteProvisionContainer(instanceId: String, service: Service, plan: Plan): DynamicContainer {
    return dynamicContainer("Deleting Provision and Polling afterwards",
        listOf(
            dynamicTest("deleting Provision") {
              val statusCode = provisionRequestRunner.runDeleteProvisionRequestAsync(instanceId, service.id, plan.id)
              assertTrue("statusCode should be 200 or 202 but was $statusCode.") { statusCode in listOf(200, 202) }
              if (statusCode == 202) {
                provisionRequestRunner.waitForFinish(instanceId, 410)
              }
            }
        ))
  }

  private fun validBindingContainer(binding: BindingBody, instanceId: String, bindingId: String): DynamicContainer {
    return dynamicContainer("Running PUT Binding and DELETE Binding afterwards", listOf(
        dynamicTest("PUT Binding") {
          bindingRequestRunner.runPutBindingRequest(binding, 201, instanceId, bindingId)
        },
        dynamicTest("DELETE Binding") {
          bindingRequestRunner.runDeleteBindingRequest(binding.service_id, binding.plan_id, 200, instanceId, bindingId)
        }
    ))
  }

  private fun validProvisionContainer(instanceId: String, provision: ProvisionBody.ValidProvisioning): DynamicContainer {
    return dynamicContainer("Provision and in case of a Async SB polling, for later binding", listOf(
        dynamicTest("Running Valid Provision with InstanceId $instanceId") {
          val statusCode = provisionRequestRunner.runPutProvisionRequestAsync(instanceId, provision)
          assertTrue("expected status code 200, 201, 202 but was $statusCode") { statusCode in listOf(200, 201, 202) }

          if (statusCode == 202) {
            assert(provisionRequestRunner.waitForFinish(instanceId, 200) == "succeeded")
          }
        }
    ))
  }
}
