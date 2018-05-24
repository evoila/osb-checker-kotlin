package de.evoila.osb.checker.tests

import com.greghaskins.spectrum.Spectrum.*
import de.evoila.osb.checker.request.CatalogRequestRunner
import org.springframework.beans.factory.annotation.Autowired


class CatalogTests : TestBase() {


  init {
    describe("Testing the catalog") {
      beforeAll {
        wire()
      }

      it("should return list of registered service classes as JSON payload") {
        catalogRequestRunner.correctRequestAndValidateResponse()
      }
    }
  }
}