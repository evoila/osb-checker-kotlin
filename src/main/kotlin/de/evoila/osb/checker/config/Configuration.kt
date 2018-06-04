package de.evoila.osb.checker.config

import java.util.*
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import kotlin.collections.HashMap

@Component
@ConfigurationProperties(prefix = "config")
class Configuration {

  lateinit var url: String
  var port: Int = 80
  lateinit var apiVersion: String
  lateinit var user: String
  lateinit var password: String
  lateinit var token: String
  var usingAppGuid: Boolean = true
  val parameters: HashMap<String, HashMap<String, Any>> = hashMapOf()

  companion object {
    val NOT_AN_ID = UUID.randomUUID().toString()
  }
}