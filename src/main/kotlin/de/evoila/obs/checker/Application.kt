package de.evoila.obs.checker


import de.evoila.obs.checker.request.bodies.ProvisionRequestBody
import de.evoila.obs.checker.tests.CatalogTests
import de.evoila.obs.checker.tests.ProvisionTests
import io.restassured.RestAssured
import io.restassured.RestAssured.*
import org.slf4j.LoggerFactory


class Application {

  fun execute() {
    setup()
    val catalog = CatalogTests.runAll(log, token)


    val provisionRequestBody = ProvisionRequestBody.Valid(catalog)

    ProvisionTests(
        token = token,
        scenario = ProvisionTests.Scenario.NEW,
        provisionRequestBody = provisionRequestBody
    ).runAll()

  }


  private fun setup() {
    baseURI = url
    RestAssured.port = port
    authentication = basic("admin", "cloudfoundry")
  }


  companion object {
    private const val url = "http://osb-couchdb-ng-test.cf.dev.eu-de-central.msh.host"
    private const val port = 80
    const val apiVersion = "2.13"
    private const val user = "admin"
    private const val password = "cloudfoundry"
    // val token = Base64.getEncoder().encode("$user:$password".toByteArray()).toString()
    val token = "Basic YWRtaW46Y2xvdWRmb3VuZHJ5"
    private val log = LoggerFactory.getLogger(Application::class.java)
  }
}

fun main(args: Array<String>) {
  val application = Application()

  application.execute()
}

