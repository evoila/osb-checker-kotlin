package de.evoila.osb.checker.config

import java.util.*
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import kotlin.collections.HashMap

@Component
@ConfigurationProperties(prefix = "config")
class Configuration {

  val parameters: HashMap<String, HashMap<String, Any>> = hashMapOf()

  companion object {

    var url: String = "http://osb-autoscaler.cf.dev.eu-de-central.msh.host"
    var port: Int = 80
    var apiVersion: String = "2.13"
    var user: String = "admin"
    var password: String = "cloudfoundry"
    var token: String? = null
    var serviceKeysFlag = true

    val NOT_AN_ID = UUID.randomUUID().toString()

  }
}