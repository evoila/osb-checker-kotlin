package de.evoila.osb.checker.request.bodies

import de.evoila.osb.checker.response.Catalog

abstract class ProvisionBody : RequestBody() {

  class ValidProvisioning(
      var service_id: String?,
      var plan_id: String?,
      var organization_guid: String? = "A_Random_Guid",
      var space_guid: String? = "A_GUID_from_SPACE!!"
  ) : ProvisionBody() {

    constructor(catalog: Catalog) : this(
        service_id = getServiceId(catalog),
        plan_id = getPlanIdForUsedServiceId(catalog)
    )
  }

  class NoServiceFieldProvisioning(
      var plan_id: String?,
      var organization_guid: String? = "A_Random_Guid",
      var space_guid: String? = "A_GUID_from_SPACE!!"
  ) : ProvisionBody() {

    constructor(catalog: Catalog) : this(
        plan_id = getPlanIdForUsedServiceId(catalog)
    )
  }

  class NpPlanFieldProvisioning(
      var service_id: String?,
      var organization_guid: String? = "A_Random_Guid",
      var space_guid: String? = "A_GUID_from_SPACE!!"
  ) : ProvisionBody() {

    constructor(catalog: Catalog) : this(
        service_id = getServiceId(catalog)
    )
  }

  class NoOrgFieldProvisioning(
      var service_id: String?,
      var plan_id: String?,
      var space_guid: String? = "A_GUID_from_SPACE!!"
  ) : ProvisionBody() {

    constructor(catalog: Catalog) : this(
        service_id = getServiceId(catalog),
        plan_id = getPlanIdForUsedServiceId(catalog)
    )
  }

  class NoSpaceFieldProvisioning(
      var service_id: String?,
      var plan_id: String?,
      var organization_guid: String? = "A_Random_Guid"
  ) : ProvisionBody() {

    constructor(catalog: Catalog) : this(
        service_id = getServiceId(catalog),
        plan_id = getPlanIdForUsedServiceId(catalog)
    )
  }
}