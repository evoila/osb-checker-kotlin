package de.evoila.osb.checker.tests

import de.evoila.osb.checker.response.catalog.Plan
import de.evoila.osb.checker.response.catalog.Service
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory
import java.util.*


@DisplayName(value = "Database Consistency Check")
class DataConsistencyJUnit5 : BindingTestBase() {

    @TestFactory
    @DisplayName(value = "Ensure a clean deletion of instances with bindings.")
    fun checkIfDeletionAreComplete(): List<DynamicNode> {
        val instanceId = UUID.randomUUID().toString()
        val bindingId = UUID.randomUUID().toString()

        return configuration.initCustomCatalog(catalogRequestRunner.correctRequest()).services.flatMap { service ->
            service.plans.map { plan ->
                val bindable = planIsBindable(service, plan)
                val dynamicContainers = mutableListOf(createDynamicContainer(
                        bindable, service, plan, instanceId, bindingId, selectMessage(bindable, true)))
                dynamicContainers.add(createDynamicContainer(
                        bindable = bindable,
                        service = service,
                        plan = plan,
                        instanceId = instanceId,
                        bindingId = bindingId,
                        message = selectMessage(bindable, false)))
                dynamicContainer("Test if reusing instance and binding ids is possible," +
                        " if service instance with binding gets deleted", dynamicContainers)
            }
        }
    }

    fun createDynamicContainer(bindable: Boolean,
                               service: Service,
                               plan: Plan,
                               instanceId: String,
                               bindingId: String,
                               message: String
    ): DynamicContainer {
        val dynamicContainers = setUpValidProvisionRequestTest(service, plan, instanceId)
        if (bindable) {
            val bindingBody = setUpValidBindingBody(service, plan)
            dynamicContainers.add(dynamicContainer("Binding on bindable service instance.",
                    bindingContainers.createValidBindingTests(bindingId, bindingBody, instanceId, plan)
            ))
        }
        dynamicContainers.add(bindingContainers.validDeleteProvisionContainer(instanceId, service, plan))

        return dynamicContainer(message, dynamicContainers)
    }

    fun selectMessage(bindable: Boolean, first: Boolean): String {
        return if (first) {
            if (bindable) {
                PROVISION_WITH_BINDING_FIRST_INSTANCE
            } else {
                PROVISION_NO_BINDING_FIRST_INSTANCE
            }
        } else {
            if (bindable) {
                PROVISION_WITH_BINDING_SECOND_INSTANCE
            } else {
                PROVISION_NO_BINDING_SECOND_INSTANCE
            }
        }
    }

    companion object {
        const val PROVISION_NO_BINDING_FIRST_INSTANCE = "Creating first provision and delete it afterwards."
        const val PROVISION_NO_BINDING_SECOND_INSTANCE = "Attempting to provision and delete it again."
        const val PROVISION_WITH_BINDING_FIRST_INSTANCE = "Creating first provision, bind on it and delete the service instance afterwards."
        const val PROVISION_WITH_BINDING_SECOND_INSTANCE = "Attempting to provision, bind and delete the service instance again."
    }
}