package de.evoila.osb.checker.request.bodies

import java.util.*

abstract class BindingBody(
    val service_id: String?,
    val plan_id: String?,
    var parameters: Map<String, Any>? = null
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