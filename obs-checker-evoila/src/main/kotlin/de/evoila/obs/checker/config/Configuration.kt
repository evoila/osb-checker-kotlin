package de.evoila.obs.checker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "checker")
class Configuration {

  lateinit var url: String
  var port: Int = 8080 //setting default to 8080
  lateinit var apiVersion: String
  lateinit var user: String
  lateinit var password: String
  lateinit var authentication: String
}