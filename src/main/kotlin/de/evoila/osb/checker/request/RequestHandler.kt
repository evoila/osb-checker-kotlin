package de.evoila.osb.checker.request

import de.evoila.osb.checker.config.Configuration


abstract class RequestHandler(val configuration: Configuration) {

  val validHeaders: MutableMap<String, Any> = mutableMapOf()


  init {
    validHeaders["X-Broker-API-Version"] = configuration.apiVersion
    validHeaders["Authorization"] = configuration.correctToken

    if (configuration.useRequestIdentity) {
      validHeaders["X-Broker-API-Request-Identity"] = FIX_GUID
    }
  }

  companion object {
    private const val FIX_GUID = "0a54bae0-b3ae-4b90-953e-155653c38106"
  }
}
