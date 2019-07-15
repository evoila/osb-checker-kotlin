package de.evoila.osb.checker.config

import de.evoila.osb.checker.response.Catalog
import de.evoila.osb.checker.response.Plan
import de.evoila.osb.checker.response.Service
import java.util.*
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import kotlin.collections.HashMap
import kotlin.test.assertTrue

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

  var skipTLSVerification: Boolean = false
  var usingAppGuid: Boolean = true
  val provisionParameters: HashMap<String, HashMap<String, Any>> = hashMapOf()
  val bindingParameters: HashMap<String, HashMap<String, Any>> = hashMapOf()

  var services = mutableListOf<CustomServices>()

  @PostConstruct
  fun validateApiVersion() {
    assertTrue(apiVersion in supportedApiVersions, noSupportedApiVersion)
  }

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
                      description = "Plan-Description",
                      metadata = null,
                      plan_updatable = null
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
      var bindable: Boolean? = null
    }
  }

  companion object {
    val notAnId = UUID.randomUUID().toString()
    private val supportedApiVersions = listOf(2.13, 2.14)
    private val noSupportedApiVersion = "You entered a not supported Api Version. Please use one of the following: $supportedApiVersions"
  }
}