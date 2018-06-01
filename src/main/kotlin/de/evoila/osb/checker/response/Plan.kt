package de.evoila.osb.checker.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class Plan {

  var id: String = ""
  var name: String = ""
  var description: String? = null
  var plan_updatable = false
  var bindable: Boolean? = null
  var metadata = PlanMetadata()
}