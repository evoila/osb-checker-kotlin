package de.evoila.osb.checker.request.bodies

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import de.evoila.osb.checker.response.catalog.MaintenanceInfo
import de.evoila.osb.checker.response.catalog.Plan
import de.evoila.osb.checker.response.catalog.Service
import java.util.*
import kotlin.collections.HashMap

abstract class ProvisionBody : RequestBody {

    data class ValidProvisioning(
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            @JsonProperty(value = "service_id")
            var serviceId: String,
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            @JsonProperty(value = "plan_id")
            var planId: String,
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            @JsonProperty(value = "organization_guid")
            var organizationGuid: String = UUID.randomUUID().toString(),
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            @JsonProperty(value = "space_guid")
            var spaceGuid: String = UUID.randomUUID().toString(),
            @JsonInclude(JsonInclude.Include.NON_NULL)
            var parameters: Map<String, Any>? = null,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @JsonProperty(value = "maintenance_info")
            var maintenanceInfo: MaintenanceInfo? = null,
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            var context: HashMap<String, Any> = hashMapOf()
    ) : ProvisionBody(), SwappableServiceAndPlan<ValidProvisioning> {

        constructor(service: Service, plan: Plan) : this(
                serviceId = service.id,
                planId = plan.id
        )

        constructor(service: Service, plan: Plan, maintenance_info: MaintenanceInfo) : this(
                serviceId = service.id,
                planId = plan.id,
                maintenanceInfo = maintenance_info
        )

        fun setContextUpdate(type: ContextObjectType) {
            return when (type) {
                ContextObjectType.CLOUDFOUNDRY -> {
                    this.context["platform"] = "cloudfoundry"
                    this.context["organization_guid"] = this.organizationGuid
                    this.context["organization_name"] = "osb-api"
                    this.context["space_guid"] = this.spaceGuid
                    this.context["space_name"] = "osb-checker-kotlin"
                    this.context["instance_name"] = "testing instance"
                }
                ContextObjectType.KUBERNETES -> {
                    this.context["platform"] = "kubernetes"
                    this.context["namespace"] = "osb-checker"
                    this.context["clusterid"] = UUID.randomUUID().toString()
                }
                ContextObjectType.NONE -> return
            }
        }

        override fun serviceIdEquals(otherServiceId: String): Boolean = this.serviceId == otherServiceId

        override fun swapServiceIdAndPlanId(service: Service): ValidProvisioning {
            return this.copy(serviceId = service.id, planId = service.plans.first().id)
        }

        override fun swapPlanId(plan: Plan): ValidProvisioning {
            return this.copy(planId = plan.id)
        }

        override fun extractPlanId(): String? = this.planId
    }

    data class NoPlanFieldProvisioning(
            var service_id: String?,
            var organization_guid: String? = UUID.randomUUID().toString(),
            var space_guid: String? = UUID.randomUUID().toString()
    ) : ProvisionBody() {


        constructor(service: Service) : this(
                service_id = service.id
        )

    }

    data class NoServiceFieldProvisioning(
            var plan_id: String?,
            var organization_guid: String? = UUID.randomUUID().toString(),
            var space_guid: String? = UUID.randomUUID().toString()
    ) : ProvisionBody() {

        constructor(plan: Plan) : this(
                plan_id = plan.id
        )

    }

    data class NoOrgFieldProvisioning(
            var service_id: String?,
            var plan_id: String?,
            var space_guid: String? = UUID.randomUUID().toString()
    ) : ProvisionBody() {

        constructor(service: Service, plan: Plan) : this(
                service_id = service.id,
                plan_id = plan.id
        )

    }

    data class NoSpaceFieldProvisioning(
            var service_id: String?,
            var plan_id: String?,
            var organization_guid: String? = UUID.randomUUID().toString()
    ) : ProvisionBody() {

        constructor(service: Service, plan: Plan) : this(
                service_id = service.id,
                plan_id = plan.id
        )

    }

    enum class ContextObjectType {
        KUBERNETES, CLOUDFOUNDRY, NONE
    }
}
