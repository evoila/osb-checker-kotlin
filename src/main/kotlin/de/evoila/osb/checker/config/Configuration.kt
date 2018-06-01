package de.evoila.osb.checker.config

import java.util.*

object Configuration {

  var url: String = "http://localhost"
  var port: Int = 80
  var apiVersion: String = "2.13"
  var user: String = "admin"
  var password: String = "cloudfoundry"
  var token: String? = null
  var serviceKeysFlag = true
  val NOT_AN_ID = UUID.randomUUID().toString()
}