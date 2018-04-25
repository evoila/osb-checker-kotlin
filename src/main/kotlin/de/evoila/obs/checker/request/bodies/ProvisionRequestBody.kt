package de.evoila.obs.checker.request.bodies

import com.fasterxml.jackson.annotation.JsonProperty
import de.evoila.obs.checker.response.Catalog
import java.io.Serializable

class ProvisionRequestBody(
    @JsonProperty("service_Id")
    val serviceId: String,
    @JsonProperty("plan_Id")
    val planId: String
) : Serializable {


  constructor(catalog: Catalog) : this(
      getServiceId(catalog),
      getPlanIdForUsedServiceId(catalog)
  )


  companion object {
    fun getServiceId(catalog: Catalog): String {
      return catalog.services[0].id

    }

    fun getPlanIdForUsedServiceId(catalog: Catalog): String {
      return catalog.services[0].plans[0].id
    }
  }
}