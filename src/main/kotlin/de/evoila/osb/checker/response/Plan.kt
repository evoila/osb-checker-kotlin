package de.evoila.osb.checker.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Plan(
    val id: String,
    val name: String,
    val description: String,
    val plan_updatable: String?,
    val bindable: Boolean?,
    val metadata: PlanMetadata?,
    @JsonProperty("maximum_polling_duration")
    val maximum_polling_duration: Int = 86400
)