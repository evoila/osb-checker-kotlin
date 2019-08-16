package de.evoila.osb.checker.response.catalog

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Plan(
    val id: String,
    val name: String,
    val description: String,
    val plan_updatable: String?,
    val bindable: Boolean?,
    val metadata: PlanMetadata?
)