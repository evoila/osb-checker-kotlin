package de.evoila.osb.checker.response

import com.fasterxml.jackson.annotation.JsonProperty

data class DashboardClient(
    val id: String?,
    @JsonProperty("redirect_uri")
    val redirectUri: String?,
    val secret: String?
)