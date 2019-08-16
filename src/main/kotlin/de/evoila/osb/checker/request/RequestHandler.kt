package de.evoila.osb.checker.request

import de.evoila.osb.checker.config.Configuration


abstract class RequestHandler(val configuration: Configuration) {

  val validHeaders: MutableMap<String, Any> = mutableMapOf()

  init {
    validHeaders["X-Broker-API-Version"] = configuration.apiVersion
    validHeaders["Authorization"] = configuration.correctToken

    if (configuration.useRequestIdentity) {
      validHeaders["X-Broker-API-Request-Identity"] = Configuration.FIX_GUID
    }
  }
}
