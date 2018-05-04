package de.evoila.osb.checker.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class LastOperationResponse {

  lateinit var state: String
  var description: String? = null
}