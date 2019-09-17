package de.evoila.osb.checker.config

import de.evoila.osb.checker.response.catalog.Catalog
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
    var originatingIdentity: OriginatingIdentity? = null
    var useRequestIdentity: Boolean = false
    var skipTLSVerification: Boolean = false
    //TODO add optional fields for not using appGUID and it should fail
    var usingAppGuid: Boolean = true
    val provisionParameters: HashMap<String, HashMap<String, Any>> = hashMapOf()
    val bindingParameters: HashMap<String, HashMap<String, Any>> = hashMapOf()
    var services = mutableListOf<CustomService>()

    fun initCustomCatalog(fullCatalog: Catalog): Catalog {
        return if (services.isNotEmpty()) {
            fullCatalog.copy(
                    services = fullCatalog.services.filter { service ->
                        services.firstOrNull { service.id == it.id }?.let { true } ?: false
                    }.map {
                        it.copy(
                                plans = it.plans.filter { plan ->
                                    val customService = services.first { customService -> customService.id == it.id }
                                    customService.plans.firstOrNull { customPlan -> customPlan.id == plan.id }?.let { true }
                                            ?: false
                                }
                        )
                    }
            )
        } else fullCatalog
    }

    class OriginatingIdentity {
        var platform: String = ""
        var value: Map<String, Any> = hashMapOf()
    }

    class CustomService {
        lateinit var id: String
        var plans = mutableListOf<CustomPlan>()

        class CustomPlan {
            lateinit var id: String
        }
    }

    companion object {
        val notAnId = UUID.randomUUID().toString()
    }
}