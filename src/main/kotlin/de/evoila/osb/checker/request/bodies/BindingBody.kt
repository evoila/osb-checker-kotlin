package de.evoila.osb.checker.request.bodies

import java.util.*

abstract class BindingBody(
    var service_id: String?,
    var plan_id: String?
) : RequestBody {

  class ValidBinding(
      service_id: String?,
      plan_id: String?
  ) : BindingBody(service_id, plan_id)

  class ValidBindingWithAppGuid(
      service_id: String?,
      plan_id: String?,
      val app_guid: String = UUID.randomUUID().toString()
  ) : BindingBody(service_id, plan_id)
}