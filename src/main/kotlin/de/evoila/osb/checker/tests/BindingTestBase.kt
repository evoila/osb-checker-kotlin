package de.evoila.osb.checker.tests

import de.evoila.osb.checker.request.BindingRequestRunner
import de.evoila.osb.checker.request.bodies.BindingBody
import de.evoila.osb.checker.request.bodies.ProvisionBody
import de.evoila.osb.checker.response.catalog.Plan
import de.evoila.osb.checker.response.catalog.Service
import de.evoila.osb.checker.tests.containers.BindingContainers
import org.junit.jupiter.api.DynamicContainer
import org.springframework.beans.factory.annotation.Autowired
import java.util.*


abstract class BindingTestBase : TestBase() {

    @Autowired
    lateinit var bindingRequestRunner: BindingRequestRunner
    @Autowired
    lateinit var bindingContainers: BindingContainers

    fun setUpValidProvisionRequestTest(service: Service,
                                       plan: Plan,
                                       instanceId: String
    ): MutableList<DynamicContainer> {
        return mutableListOf(
                bindingContainers.validProvisionContainer(
                        instanceId = instanceId,
                        plan = plan,
                        provision = setUpValidProvisionBody(service, plan),
                        isRetrievable = configuration.apiVersion > 2.13 && (service.instancesRetrievable
                                ?: false),
                        serviceName = service.name,
                        planName = plan.name
                )
        )
    }

    fun setUpValidProvisionBody(service: Service, plan: Plan): ProvisionBody.ValidProvisioning {
        val provision = if (configuration.apiVersion >= 2.15 && plan.maintenanceInfo != null)
            ProvisionBody.ValidProvisioning(service, plan, plan.maintenanceInfo)
        else ProvisionBody.ValidProvisioning(service, plan)

        configuration.provisionParameters.let {
            if (it.containsKey(plan.id)) {
                provision.parameters = it[plan.id]
            }
        }

        return provision
    }

    fun setUpValidBindingBody(service: Service, plan: Plan): BindingBody {
        val binding = if (needsAppGuid(plan)) BindingBody(
                serviceId = service.id,
                planId = plan.id,
                appGuid = UUID.randomUUID().toString()
        )
        else BindingBody(serviceId = service.id, planId = plan.id)
        configuration.bindingParameters.let {
            if (it.containsKey(plan.id)) {
                binding.parameters = it[plan.id]
            }
        }

        return binding
    }

    fun setUpServiceIdMissingBindingBody(plan: Plan): BindingBody {

        return if (needsAppGuid(plan)) {
            BindingBody(null, plan.id, UUID.randomUUID().toString())
        } else BindingBody(null, plan.id)
    }

    fun setUpPlanIdMissingBindingBody(service: Service, plan: Plan): BindingBody {

        return if (needsAppGuid(plan)) {
            BindingBody(service.id, null, UUID.randomUUID().toString())
        } else BindingBody(service.id, null)
    }

    fun planIsBindable(service: Service, plan: Plan): Boolean = plan.bindable ?: service.bindable

    fun needsAppGuid(plan: Plan): Boolean = plan.metadata?.customParameters?.usesServicesKeys
            ?: configuration.usingAppGuid
}
