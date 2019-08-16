package de.evoila.osb.checker.response.operations

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class AsyncProvision(
    @JsonProperty("dashboard_url")
    val dashboardUrl: String?,
    @JsonProperty("operation")
    val operation: String?
)