package de.evoila.osb.checker.tests


import de.evoila.osb.checker.request.BindingRequestRunner
import de.evoila.osb.checker.request.ProvisionRequestRunner
import de.evoila.osb.checker.request.bodies.BindingBody
import de.evoila.osb.checker.request.bodies.ProvisionBody
import de.evoila.osb.checker.response.Plan
import de.evoila.osb.checker.response.Service
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BindingJUnit5 : TestBase() {

  @Autowired
  lateinit var bindingRequestRunner: BindingRequestRunner
  @Autowired
  lateinit var provisionRequestRunner: ProvisionRequestRunner

  @TestFactory
  fun runValidBindings(): List<DynamicNode> {

    val catalog = configuration.initCustomCatalog() ?: catalogRequestRunner.correctRequest()
    val dynamicNodes = mutableListOf<DynamicNode>()

    catalog.services.forEach { service ->
      service.plans.forEach { plan ->

        val instanceId = UUID.randomUUID().toString()
        val bindingId = UUID.randomUUID().toString()
        val needsAppGuid: Boolean = plan.metadata?.customParameters?.usesServicesKeys ?: configuration.usingAppGuid

        val provision = ProvisionBody.ValidProvisioning(service, plan)
        val binding = if (needsAppGuid) BindingBody.ValidBindingWithAppGuid(service.id, plan.id) else BindingBody.ValidBinding(service.id, plan.id)


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

        val testContainers = mutableListOf(validProvisionContainer(instanceId, plan.name, provision,
            service.instancesRetrievable ?: false))

        val bindable = plan.bindable ?: service.bindable

        if (bindable) {
          testContainers.add(validBindingContainer(binding, instanceId, bindingId, service.bindingsRetrievable
              ?: false))
        }

        testContainers.add(validDeleteProvisionContainer(instanceId, service, plan))

        dynamicNodes.add(
            dynamicContainer("Running a valid provision if the service is bindable a valid binding. Deleting both afterwards. In case of a async service broker poll afterwards.", testContainers)
        )
      }
    }
    return dynamicNodes
  }

  @TestFactory
  fun runInvalidBindingAttempts(): List<DynamicNode> {

    val catalog = configuration.initCustomCatalog() ?: catalogRequestRunner.correctRequest()
    val service = catalog.services.first()
    val plan = service.plans.first()
    val bindable = plan.bindable ?: service.bindable
    val needsAppGuid: Boolean = plan.metadata?.customParameters?.usesServicesKeys ?: configuration.usingAppGuid

    if (!bindable) {
      return emptyList()
    }

    val provision = ProvisionBody.ValidProvisioning(catalog.services.first(), plan)
    val instanceId = UUID.randomUUID().toString()
    val bindingId = UUID.randomUUID().toString()

    val dynamicNodes = mutableListOf<DynamicNode>()

    dynamicNodes.add(
        validProvisionContainer(instanceId, plan.name, provision, service.instancesRetrievable ?: false)
    )

    val invalidBindings = mutableListOf<DynamicNode>()


    listOf(
        TestCase(
            requestBody =
            if (needsAppGuid) BindingBody.ValidBindingWithAppGuid(null, plan.id) else BindingBody.ValidBinding(null, plan.id),
            message = "should reject if missing service_id"
        ),
        TestCase(
            requestBody = if (needsAppGuid) BindingBody.ValidBindingWithAppGuid(service.id, null) else BindingBody.ValidBinding(service.id, null),
            message = "should reject if missing plan_id"
        )
    ).forEach {
      invalidBindings.add(
          dynamicTest("PUT ${it.message}")
          { assertTrue { 400 == bindingRequestRunner.runPutBindingRequest(it.requestBody, instanceId, bindingId) } }
      )
      invalidBindings.add(
          dynamicTest("DELETE ${it.message}")
          {
            val bindingRequestBody = it.requestBody

            bindingRequestRunner.runDeleteBindingRequest(
                serviceId = bindingRequestBody.service_id,
                planId = bindingRequestBody.plan_id,
                instanceId = instanceId,
                bindingId = bindingId)
          }
      )
    }

    dynamicNodes.add(dynamicContainer("Running invalid bindings", invalidBindings))
    dynamicNodes.add(
        validDeleteProvisionContainer(instanceId, service, plan)
    )
    return dynamicNodes
  }

  private fun validDeleteProvisionContainer(instanceId: String, service: Service, plan: Plan): DynamicContainer {
    return dynamicContainer("Deleting provision",
        listOf(
            dynamicTest("DELETE provision and if the service broker is async polling afterwards") {
              val statusCode = provisionRequestRunner.runDeleteProvisionRequestAsync(instanceId, service.id, plan.id)
              assertTrue("StatusCode should be 200 or 202 but was $statusCode.") { statusCode in listOf(200, 202) }

              if (statusCode == 202) {
                provisionRequestRunner.waitForFinish(instanceId, 410)
              }
            }
        ))
  }

  private fun validBindingContainer(binding: BindingBody, instanceId: String, bindingId: String, isRetrievable: Boolean): DynamicContainer {

    val bindingTests = listOf(
        dynamicTest("Running a valid binding with bindingId $bindingId") {
          val statusCode = bindingRequestRunner.runPutBindingRequest(binding, instanceId, bindingId)
          assertTrue { statusCode in listOf(201, 202) }

          if (statusCode == 202) {
            val state = bindingRequestRunner.waitForFinish(instanceId, bindingId, 200)
            assertTrue("Expected the final polling state to be \"succeeded\" but was $state") { "succeeded" == state }
          }
        }
    )

    return dynamicContainer("Running PUT binding and DELETE binding afterwards", if (isRetrievable) {
      bindingTests.plus(listOf(validRetrievableBindingContainer(instanceId, bindingId), validDeleteContainer(binding, instanceId, bindingId)))
    } else {
      bindingTests.plus(validDeleteContainer(binding, instanceId, bindingId))
    })
  }

  private fun validDeleteContainer(binding: BindingBody, instanceId: String, bindingId: String): DynamicTest = dynamicTest("Deleting binding with bindingId $bindingId") {
    val statusCode = bindingRequestRunner.runDeleteBindingRequest(binding.service_id, binding.plan_id, instanceId, bindingId)

    assertTrue("StatusCode should be 200 or 202 but was $statusCode.") { statusCode in listOf(200, 202) }

    if (statusCode == 202) {
      bindingRequestRunner.waitForFinish(instanceId, bindingId, 410)
    }
  }

  private fun validRetrievableBindingContainer(instanceId: String, bindingId: String): DynamicTest {

    return dynamicTest("Running valid GET for retrievable service binding") {
      bindingRequestRunner.runGetBindingRequest(200, instanceId, bindingId)
    }
  }

  private fun validRetrievableInstanceContainer(instanceId: String, provision: ProvisionBody.ValidProvisioning, isRetrievable: Boolean): DynamicTest {

    return dynamicTest("Running valid GET for retrievable service instance") {

      val serviceInstance = provisionRequestRunner.getProvision(instanceId, isRetrievable)

      assertNotNull(serviceInstance, "did not receive a service instance as a response")
      assertTrue("When retrieving the instance the response did not match the expected value. \n" +
          "service_id: expected ${provision.service_id} actual ${serviceInstance!!.serviceId} \n" +
          "plan_id: expected ${provision.plan_id} actual ${serviceInstance.planId}")
      {
        serviceInstance.serviceId == provision.service_id && serviceInstance.planId == provision.plan_id
      }
    }
  }

  private fun validProvisionContainer(instanceId: String, planName: String, provision: ProvisionBody.ValidProvisioning, isRetrievable: Boolean): DynamicContainer {

    val provisionTests = listOf(
        dynamicTest("Running valid PUT provision with instanceId $instanceId for service ${provision.service_id} and plan $planName id: ${provision.plan_id}") {

          val statusCode = provisionRequestRunner.runPutProvisionRequestAsync(instanceId, provision)

          assertTrue("expected status code 200, 201, 202 but was $statusCode") { statusCode in listOf(200, 201, 202) }

          if (statusCode == 202) {
            val state = provisionRequestRunner.waitForFinish(instanceId, 200)
            assertTrue("Expected the final polling state to be \"succeeded\" but was $state") { "succeeded" == state }
          }
        })

    return dynamicContainer("Provision and in case of a async service broker polling, for later binding", if (isRetrievable) {
      provisionTests.plus(validRetrievableInstanceContainer(instanceId, provision, isRetrievable))
    } else {
      provisionTests
    })
  }
}
