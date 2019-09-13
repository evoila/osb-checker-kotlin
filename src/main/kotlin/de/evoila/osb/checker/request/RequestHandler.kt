package de.evoila.osb.checker.request

import de.evoila.osb.checker.config.Configuration
import java.util.*


abstract class RequestHandler(val configuration: Configuration) {

  val validRequestHeaders: MutableMap<String, Any> = mutableMapOf()
  val expectedResponseHeaders: MutableMap<String, Any> = mutableMapOf()


  init {
    validRequestHeaders["X-Broker-API-Version"] = configuration.apiVersion
    validRequestHeaders["Authorization"] = configuration.correctToken

    if (configuration.useOriginatingIdentity) {
      validRequestHeaders[ORIGINATING_IDENTITY_KEY] = Configuration.originatingIdentityEncoded
      expectedResponseHeaders[ORIGINATING_IDENTITY_KEY] = Configuration.originatingIdentityEncoded
    }


    if (configuration.apiVersion >= 2.15 && configuration.useRequestIdentity) {
      validRequestHeaders[REQUEST_IDENTITY_KEY] = Configuration.FIX_GUID
      expectedResponseHeaders[REQUEST_IDENTITY_KEY] = Configuration.FIX_GUID
    }
  }

  companion object {
    const val ORIGINATING_IDENTITY_KEY = "X-Broker-API-Originating-Identity"
    const val REQUEST_IDENTITY_KEY = "X-Broker-API-Request-Identity"
  }
}
