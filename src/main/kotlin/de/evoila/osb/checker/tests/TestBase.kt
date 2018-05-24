package de.evoila.osb.checker.tests

import com.greghaskins.spectrum.Spectrum
import de.evoila.osb.checker.Application
import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.request.CatalogRequestRunner
import de.evoila.osb.checker.request.ProvisionRequestRunner
import de.evoila.osb.checker.response.Catalog
import io.restassured.RestAssured
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.stereotype.Service
import org.springframework.test.context.TestContextManager
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest(classes = [Application::class])
abstract class TestBase {

  @Autowired
  lateinit var catalogRequestRunner: CatalogRequestRunner
  @Autowired
  lateinit var provisionRequestRunner: ProvisionRequestRunner


  final fun cleanUp(usedIds: MutableMap<String, Provision>) {
    usedIds.forEach {
      provisionRequestRunner.runDeleteProvisionRequestAsync(
          it.key, it.value.serviceID, it.value.planId
      )
    }
  }

  final fun setupCatalog(): Catalog {
    wire()
    return catalogRequestRunner.correctRequest()
  }

  final fun wire() {

    val testContextManager = TestContextManager(this.javaClass)

    testContextManager.beforeTestClass()
    testContextManager.prepareTestInstance(this)

    Configuration.token = "Basic ${Base64.getEncoder().encodeToString("${Configuration.user}:${Configuration.password}".toByteArray())}"

    RestAssured.baseURI = Configuration.url
    RestAssured.port = Configuration.port
    RestAssured.authentication = RestAssured.basic("admin", "cloudfoundry")
  }

}

class Provision(
    val serviceID: String,
    val planId: String
)
