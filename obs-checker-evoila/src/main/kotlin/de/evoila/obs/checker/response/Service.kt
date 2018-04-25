package de.evoila.obs.checker.response

import com.fasterxml.jackson.annotation.JsonProperty

class Service {

  var id: String = ""

  var bindable: Boolean = false

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
