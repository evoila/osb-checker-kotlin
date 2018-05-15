package de.evoila.osb.checker.tests

import com.greghaskins.spectrum.Spectrum.*
import de.evoila.osb.checker.request.CatalogRequestRunner
import org.springframework.beans.factory.annotation.Autowired


class CatalogTests : TestBase() {

  @Autowired
  lateinit var catalogRequestRunner: CatalogRequestRunner

  init {
    describe("Testing the catalog") {
      beforeAll {
        wireAndUnwire()
      }

      it("should return list of registered service classes as JSON payload") {
        catalogRequestRunner.correctRequestAndValidateResponse()
      }
    }
  }
}