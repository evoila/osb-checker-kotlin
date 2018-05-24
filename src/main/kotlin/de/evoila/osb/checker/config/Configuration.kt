package de.evoila.osb.checker.config

object Configuration {

  var url: String = "http://localhost"
  var port: Int = 8080
  var apiVersion: String = "2.13"
  var user: String = "admin"
  var password: String = "cloudfoundry"
  var token: String? = null
  var maxServices = 2
  const val NOT_AN_ID = "Delete_me_if_i_get_deployed"
}