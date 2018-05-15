package de.evoila.osb.checker.tests

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.Spectrum.*
import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.config.TestConfig
import io.restassured.RestAssured
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestContextManager
import org.springframework.transaction.annotation.Transactional


@RunWith(Spectrum::class)
@Import(TestConfig::class)
@AutoConfigureMockMvc
@SpringBootTest
abstract class TestBase {

  @Autowired
  lateinit var configuration: Configuration


  @Transactional
  final fun wireAndUnwire(transactional: Boolean = true) {

    try {
      val testContextManager = TestContextManager(this.javaClass)
      val wire = this.javaClass.getMethod("wireAndUnwire", Boolean::class.java)

      beforeAll {
        testContextManager.prepareTestInstance(this)

        testContextManager.beforeTestClass()

        if (transactional) {
          testContextManager.beforeTestMethod(this, wire)
        }

        RestAssured.baseURI = configuration.url
        RestAssured.port = configuration.port
        RestAssured.authentication = RestAssured.basic("admin", "cloudfoundry")
      }

      afterAll {
        testContextManager.afterTestMethod(this, wire, null)
        testContextManager.afterTestClass()
      }
    } catch (e: Exception) {
    }
  }
}