package de.evoila.osb.checker.config

import java.util.*

object Configuration {

  const val url = "http://osb-samba-ng-test.cf.dev.eu-de-central.msh.host"
  const val port = 80
  const val apiVersion = "2.13"
  const val user = "admin"
  const val password = "cloudfoundry"
  // val token = Base64.getEncoder().encode("$user:$password".toByteArray()).toString()
  const val token = "Basic YWRtaW46Y2xvdWRmb3VuZHJ5"
  val INSTANCE_ID = UUID.randomUUID().toString()
  val BINDING_ID = UUID.randomUUID().toString()

}