package de.evoila.osb.checker.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Service(
    val name: String,
    val id: String,
    val description: String,
    val requires: List<String>?,
    var tags: List<String>?,
    val bindable: Boolean,
    val metadata: ServiceMetadata?,
    @JsonProperty("dashboard_client")
    val dashboardClient: DashboardClient?,
    @JsonProperty("plan_updatable")
    val planUpdatable: Boolean?,
    @JsonProperty("instances_retrievable")
    val instancesRetrievable: Boolean?,
    @JsonProperty("binding_retrievable")
    val bindingRetrievable: Boolean?,

    val plans: List<Plan>
)
