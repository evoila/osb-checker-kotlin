package de.evoila.osb.checker.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class LastOperationResponse {

  var state: String? = null
  var description: String? = null
}