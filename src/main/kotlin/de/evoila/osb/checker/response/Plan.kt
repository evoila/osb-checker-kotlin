package de.evoila.osb.checker.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import de.evoila.osb.checker.request.bodies.ProvisionBody

@JsonIgnoreProperties(ignoreUnknown = true)
data class Plan(
    val id: String,
    val name: String,
    val description: String,
    val plan_updatable: String?,
    val bindable: Boolean?,
    val metadata: PlanMetadata?,
    @JsonProperty("maximum_polling_duration")
    val maximumPollingDuration: Int = 86400,
    val maintenanceInfo: MaintenanceInfo?
)