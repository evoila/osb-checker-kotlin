package de.evoila.osb.checker.request.bodies

import de.evoila.osb.checker.response.Catalog
import java.io.Serializable


abstract class ProvisionRequestBody : Serializable {

  class Valid(
      var service_id: String?,
      var plan_id: String?,
      var organization_guid: String? = "A_Random_Guid",
      var space_guid: String? = "A_GUID_from_SPACE!!"
  ) : ProvisionRequestBody() {

    constructor(catalog: Catalog) : this(
        getServiceId(catalog),
        getPlanIdForUsedServiceId(catalog)
    )
  }

  class Invalid : ProvisionRequestBody() {
    val no_a_service_id = "total Nonesense"
  }

  class Update(
      var service_id: String?
  ) : ProvisionRequestBody()


  companion object {
    fun getServiceId(catalog: Catalog): String {
      return catalog.services[0].id

    }

    fun getPlanIdForUsedServiceId(catalog: Catalog): String {
      return catalog.services[0].plans[0].id
    }
  }
}