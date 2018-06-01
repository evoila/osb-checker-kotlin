package de.evoila.osb.checker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("stuff")
class Configuration {

  var customParameters: Map<String, Map<String, Any>>? = null

  companion object {

    var url: String = "http://localhost"
    var port: Int = 8080
    var apiVersion: String = "2.13"
    var user: String = "admin"
    var password: String = "cloudfoundry"
    var token: String? = null
    var serviceKeysFlag = false

    const val NOT_AN_ID = "Delete_me_if_i_get_deployed"
  }
}