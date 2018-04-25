package de.evoila.obs.checker.request.bodies

import com.fasterxml.jackson.annotation.JsonProperty
import de.evoila.obs.checker.response.Catalog
import java.io.Serializable

class ProvisionRequestBody(
    var service_id: String?,
    var plan_id: String?,
    var organization_guid: String? = "A_Random_Guid",
    var space_guid: String = "A_GUID_from_SPACE!!"


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