package de.evoila.osb.checker.tests.contract

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.Spectrum.*
import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.config.Configuration.NOT_AN_ID
import de.evoila.osb.checker.config.Configuration.token
import de.evoila.osb.checker.request.BindingRequestRunner
import de.evoila.osb.checker.request.CatalogRequestRunner
import de.evoila.osb.checker.request.ProvisionRequestRunner
import de.evoila.osb.checker.tests.TestBase
import org.junit.runner.RunWith


@RunWith(Spectrum::class)
class ContractTest : TestBase() {
  init {
    describe("Requests should contain header X-Broker-API-Version: 2.13") {

      val catalogRequestRunner = CatalogRequestRunner(token)
      val provisionRequestRunner = ProvisionRequestRunner(NOT_AN_ID)
      val bindingRequestRunner = BindingRequestRunner(NOT_AN_ID, NOT_AN_ID)


      it("GET - v2/catalog should reject with 412") {
        catalogRequestRunner.withoutHeader()
      }

      it("PUT - v2/service_instance/instance_id should reject with 412") {
        provisionRequestRunner.putWithoutHeader()
      }

      it("GET - v2/service_instance/instance_id/last_operation should reject with 412") {
        provisionRequestRunner.lastOperationWithoutHeader()
      }

      it("DELETE - v2/service_instance/instance_id should reject with 412") {
        provisionRequestRunner.deleteWithoutHeader("Invalid", "Invalid")
      }

      it("PUT - v2/service_instance/instance_id/service_binding/binding_id  should reject with 412") {
        bindingRequestRunner.putWithoutHeader()
      }

      it("DELETE - v2/service_instance/instance_id/service_binding/binding_id  should reject with 412") {
        bindingRequestRunner.deleteWithoutHeader()
      }
    }
  }
}