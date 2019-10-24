package de.evoila.osb.checker.request.bodies

import com.fasterxml.jackson.annotation.JsonInclude
import de.evoila.osb.checker.response.catalog.MaintenanceInfo
import de.evoila.osb.checker.response.catalog.Plan
import de.evoila.osb.checker.response.catalog.Service
import java.util.*
import kotlin.collections.HashMap

abstract class ProvisionBody : RequestBody {

    data class ValidProvisioning(
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            val service_id: String,
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            val plan_id: String,
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            val organization_guid: String = UUID.randomUUID().toString(),
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            val space_guid: String = UUID.randomUUID().toString(),
            @JsonInclude(JsonInclude.Include.NON_NULL)
            val maintenance_info: MaintenanceInfo? = null,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            var parameters: Map<String, Any>? = null,
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            var context: HashMap<String, Any> = hashMapOf()
    ) : ProvisionBody() {

        constructor(service: Service, plan: Plan) : this(
                service_id = service.id,
                plan_id = plan.id
        )

        constructor(service: Service, plan: Plan, maintenance_info: MaintenanceInfo) : this(
                service_id = service.id,
                plan_id = plan.id,
                maintenance_info = maintenance_info
        )

        fun setContextUpdate(type: ContextObjectType) {
            return when (type) {
                ContextObjectType.CLOUDFOUNDRY -> {
                    this.context["platform"] = "cloudfoundry"
                    this.context["organization_guid"] = this.organization_guid
                    this.context["organization_name"] = "osb-api"
                    this.context["space_guid"] = this.space_guid
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
    }

    data class NoPlanFieldProvisioning(
            val service_id: String?,
            val organization_guid: String? = UUID.randomUUID().toString(),
            val space_guid: String? = UUID.randomUUID().toString()
    ) : ProvisionBody() {

        constructor(service: Service) : this(
                service_id = service.id
        )
    }

    data class NoServiceFieldProvisioning(
            val plan_id: String?,
            val organization_guid: String? = UUID.randomUUID().toString(),
            val space_guid: String? = UUID.randomUUID().toString()
    ) : ProvisionBody() {

        constructor(plan: Plan) : this(
                plan_id = plan.id
        )
    }

    data class NoOrgFieldProvisioning(
            val service_id: String?,
            val plan_id: String?,
            val space_guid: String? = UUID.randomUUID().toString()
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