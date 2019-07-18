package de.evoila.osb.checker.tests


import de.evoila.osb.checker.request.BindingRequestRunner
import de.evoila.osb.checker.request.bodies.BindingBody
import de.evoila.osb.checker.request.bodies.ProvisionBody
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

        val testContainers = mutableListOf(
            bindingContainerFactory.validProvisionContainer(instanceId, plan, provision, configuration.apiVersion > 2.13 && (service.instancesRetrievable
                ?: false))
        )
        testContainers.add(bindingContainerFactory.validBindingContainer(binding, instanceId, bindingId, configuration.apiVersion > 2.13 && service.bindingsRetrievable ?: false, plan))
        testContainers.add(bindingContainerFactory.validDeleteProvisionContainer(instanceId, service, plan))
        dynamicNodes.add(dynamicContainer(VALID_BINDING_MESSAGE, testContainers))
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
        bindingContainerFactory.validProvisionContainer(instanceId, plan, provision, configuration.apiVersion > 2.13 && (service.instancesRetrievable
            ?: false))
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
          { bindingRequestRunner.runPutBindingRequest(it.requestBody, instanceId, bindingId, 400) }
      )
      invalidBindings.add(
          dynamicTest("DELETE ${it.message}")
          {
            val bindingRequestBody = it.requestBody
            bindingRequestRunner.runDeleteBindingRequest(bindingRequestBody.service_id, bindingRequestBody.plan_id, instanceId, bindingId, 410)
          }
      )
    }

    dynamicNodes.add(dynamicContainer("Running invalid bindings", invalidBindings))
    dynamicNodes.add(bindingContainerFactory.validDeleteProvisionContainer(instanceId, service, plan))
    return dynamicNodes
  }

  companion object {
    private const val VALID_BINDING_MESSAGE = "Running a valid provision and if the service is bindable a valid binding. Deleting both afterwards. In case of a async service broker poll afterwards."
  }
}
