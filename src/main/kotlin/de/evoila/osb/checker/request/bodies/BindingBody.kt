package de.evoila.osb.checker.request.bodies

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.annotation.JsonProperty
import de.evoila.osb.checker.response.catalog.Plan
import de.evoila.osb.checker.response.catalog.Service

data class BindingBody(
        @JsonProperty(value = "service_id")
        @JsonInclude(NON_EMPTY)
        val serviceId: String?,
        @JsonProperty(value = "plan_id")
        val planId: String?,
        @JsonInclude(NON_EMPTY)
        var parameters: Map<String, Any>?,
        @JsonInclude(NON_EMPTY)
        @JsonProperty(value = "app_guid")
        val appGuid: String?,
        @JsonProperty(value = "bind_resource")
        @JsonInclude(NON_EMPTY)
        var bindResource: BindResource?,
        @JsonInclude(NON_EMPTY)
        var context: Map<String, Any>?
) : RequestBody, SwappableServiceAndPlan<BindingBody> {

    constructor(serviceId: String?, planId: String?, appGuid: String?) : this(
            serviceId = serviceId,
            planId = planId,
            parameters = null,
            appGuid = appGuid,
            bindResource = BindResource(
                    appGuid = appGuid
            ),
            context = null
    )

    constructor(serviceId: String?, planId: String?) : this(
            serviceId = serviceId,
            planId = planId,
            parameters = null,
            appGuid = null,
            bindResource = null,
            context = null
    )

    data class BindResource(
            @JsonProperty(value = "app_guid")
            @JsonInclude(NON_EMPTY)
            val appGuid: String?,
            @JsonInclude(NON_EMPTY)
            val route: String?
    ) {
        constructor(appGuid: String?) : this(
                appGuid = appGuid,
                route = null
        )
    }

    override fun serviceIdEquals(otherServiceId: String): Boolean = serviceId == otherServiceId

    override fun swapServiceIdAndPlanId(service: Service): BindingBody {
        return this.copy(serviceId = service.id, planId = service.plans.first().id)
    }

    override fun swapPlanId(plan: Plan): BindingBody {
        return this.copy(planId = plan.id)
    }

    override fun extractPlanId(): String? = this.planId
}