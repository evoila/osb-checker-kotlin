package de.evoila.osb.checker.response.operations

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class LastOperationResponse(
    val state: String,
    val description: String?,
    val operation: String?
)