package de.evoila.osb.checker.tests

import com.greghaskins.spectrum.Spectrum
import de.evoila.osb.checker.Application
import de.evoila.osb.checker.config.Configuration
import io.restassured.RestAssured
import org.junit.runner.RunWith
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import java.util.*

@RunWith(Spectrum::class)
@AutoConfigureMockMvc
@SpringBootTest(classes = [Application::class])
abstract class TestBase {


  final fun wireAndUnwire() {

    val testContextManager = TestContextManager(this.javaClass)

    testContextManager.beforeTestClass()
    testContextManager.prepareTestInstance(this)

    Configuration.token = "Basic ${Base64.getEncoder().encodeToString("${Configuration.user}:${Configuration.password}".toByteArray())}"

    RestAssured.baseURI = Configuration.url
    RestAssured.port = Configuration.port
    RestAssured.authentication = RestAssured.basic("admin", "cloudfoundry")
  }
}