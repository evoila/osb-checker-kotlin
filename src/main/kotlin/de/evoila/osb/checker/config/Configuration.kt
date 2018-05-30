package de.evoila.osb.checker.config

object Configuration {

  var url: String = "http://osb-autoscaler.cf.dev.eu-de-central.msh.host"
  var port: Int = 80
  var apiVersion: String = "2.13"
  var user: String = "admin"
  var password: String = "cloudfoundry"
  var token: String? = null
  var maxServices = 1
  var maxPlans = 3
  const val NOT_AN_ID = "Delete_me_if_i_get_deployed"
}