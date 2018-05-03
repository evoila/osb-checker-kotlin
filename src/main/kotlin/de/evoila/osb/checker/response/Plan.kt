package de.evoila.osb.checker.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class Plan {

  var id: String = ""
  var name: String = ""
  var description: String? = null
  var vmType: String? = null
  var persistentDiskType: String? = null
}
