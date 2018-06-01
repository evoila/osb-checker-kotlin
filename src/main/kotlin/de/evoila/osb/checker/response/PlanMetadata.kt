package de.evoila.osb.checker.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class PlanMetadata {
  var connections: String? = null
  var nodes: String? = null
  var vmType: String? = null
  var persistentDiskType: String? = null
  var ingressInstanceGroup: String? = null
  var customParameters = CustomParameters()
}