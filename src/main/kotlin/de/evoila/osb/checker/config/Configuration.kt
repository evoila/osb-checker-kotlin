package de.evoila.osb.checker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.util.*


@Component
@ConfigurationProperties(prefix = "checker")
class Configuration {

  lateinit var url: String
  var port: Int = 80
  lateinit var apiVersion: String
  lateinit var user: String
  lateinit var password: String
  var token: String? = null


  companion object {
    const val NOT_AN_ID = "Delete_me_if_i_get_deployed"
  }
}