package de.evoila.osb.checker.config

object Configuration {

  const val url = "https://example-dev.cf.dev.eu-de-central.msh.host"
  const val apiVersion = "2.13"
  const val user = "admin"
  const val password = "cloudfoundry"
  // val token = Base64.getEncoder().encode("$user:$password".toByteArray()).toString()
  const val token = "Basic YWRtaW46Y2xvdWRmb3VuZHJ5"
  const val NOT_AN_ID = "Delete_me_if_i_get_deployed"
}