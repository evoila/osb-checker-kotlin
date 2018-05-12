package de.evoila.osb.checker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component


@Component
@ConfigurationProperties(prefix = "checker")
class Configuration {

  lateinit var url: String
  var port: Int = 80
  lateinit var apiVersion: String
  lateinit var user: String
  lateinit var password: String
  // val token = Base64.getEncoder().encode("$user:$password".toByteArray()).toString()

  companion object {
    const val token = "Basic YWRtaW46Y2xvdWRmb3VuZHJ5"
    const val NOT_AN_ID = "Delete_me_if_i_get_deployed"
  }
}