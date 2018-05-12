package de.evoila.osb.checker.tests

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.Spectrum.it
import de.evoila.osb.checker.Application
import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.request.CatalogRequestRunner
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [Application::class])
@RunWith(Spectrum::class)
class CatalogTests {

  init {

    Spectrum.describe("Testing the catalog") {
      val catalogRequestRunner = CatalogRequestRunner(Configuration.token)

      it("should return list of registered service classes as JSON payload") {
        catalogRequestRunner.correctRequestAndValidateResponse()
      }
    }
  }
}