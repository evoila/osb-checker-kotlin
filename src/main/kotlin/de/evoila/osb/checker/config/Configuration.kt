package de.evoila.osb.checker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.util.*

object Configuration {

  const val url = "http://osb-couchdb-ng-test.cf.dev.eu-de-central.msh.host"
  const val port = 80
  const val apiVersion = "2.13"
  const val user = "admin"
  const val password = "cloudfoundry"
  // val token = Base64.getEncoder().encode("$user:$password".toByteArray()).toString()
  val token = "Basic YWRtaW46Y2xvdWRmb3VuZHJ5"
  val INSTANCE_ID = "TEST_INSTANCE_BY_JS"
}