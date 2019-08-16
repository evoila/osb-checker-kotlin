package de.evoila.osb.checker.config

import de.evoila.osb.checker.response.catalog.Catalog
import de.evoila.osb.checker.response.catalog.Plan
import de.evoila.osb.checker.response.catalog.Service
import java.util.*
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import kotlin.collections.HashMap

@Component
@ConfigurationProperties(prefix = "config")
class Configuration {

  lateinit var url: String

  var port: Int = 80
  var apiVersion: Double = 0.0
  lateinit var user: String
  lateinit var password: String
  lateinit var correctToken: String
  lateinit var wrongUserToken: String
  lateinit var wrongPasswordToken: String

  var useRequestIdentity: Boolean = false
  var skipTLSVerification: Boolean = false
  var usingAppGuid: Boolean = true
  val provisionParameters: HashMap<String, HashMap<String, Any>> = hashMapOf()
  val bindingParameters: HashMap<String, HashMap<String, Any>> = hashMapOf()
  var services = mutableListOf<CustomServices>()

  fun initCustomCatalog(): Catalog? {
    return if (services.isNotEmpty()) {

      Catalog(
          services.map { customService ->
            Service(
                id = customService.id,
                name = "Service-Name",
                dashboardClient = null,
                bindable = customService.bindable,
                tags = null,
                metadata = null,
                planUpdatable = null,
                description = "Service-Description",
                requires = null,
                instancesRetrievable = customService.instancesRetrievable,
                bindingsRetrievable = customService.bindingRetrievable,

                plans = customService.plans.map { customPlan ->
                  Plan(
                      id = customPlan.id,
                      name = "Plan-Name",
                      bindable = customPlan.bindable,
                      description = "Plan-Description"
                  )
                }
            )
          }
      )
    } else {

      null
    }
  }

  class CustomServices {
    lateinit var id: String
    var plans = mutableListOf<CustomPlan>()
    var bindable = true
    var instancesRetrievable = false
    var bindingRetrievable = false


    class CustomPlan {
      lateinit var id: String
      var bindable: Boolean = false
    }
  }

  companion object {
    val notAnId = UUID.randomUUID().toString()
    const val FIX_GUID = "0a54bae0-b3ae-4b90-953e-155653c38106"
  }
}