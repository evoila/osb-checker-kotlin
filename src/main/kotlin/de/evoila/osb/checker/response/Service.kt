package de.evoila.osb.checker.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class Service {

  var plans: List<Plan> = listOf()

  var id: String = ""

  var bindable: Boolean = true

  var description: String = ""

  @JsonProperty("max_db_per_node")
  var maxDbPerMode: Int = 0

  var name: String = ""

  @JsonProperty("plan_updateable")
  var planUpdateable: Boolean = false

  var requires: List<String> = listOf()

  var tags: List<String> = listOf()

  @JsonProperty("dashboard_client")
  var dashbordClient: DashboardClient? = null


  var metadata: ServiceMetadata? = null
}
