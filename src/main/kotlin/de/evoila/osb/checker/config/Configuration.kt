package de.evoila.osb.checker.config


object Configuration {

  lateinit var url: String
  var port: Int = 80
  lateinit var apiVersion: String
  lateinit var user: String
  lateinit var password: String
  var token: String? = null
  const val NOT_AN_ID = "Delete_me_if_i_get_deployed"
}