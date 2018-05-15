package de.evoila.osb.checker.tests.contract

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.Spectrum.describe
import com.greghaskins.spectrum.Spectrum.it
import de.evoila.osb.checker.Application
import de.evoila.osb.checker.request.BindingRequestRunner
import de.evoila.osb.checker.request.CatalogRequestRunner
import de.evoila.osb.checker.request.ProvisionRequestRunner
import de.evoila.osb.checker.tests.TestBase
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [Application::class])
@RunWith(Spectrum::class)
class ContractTest : TestBase() {

  @Autowired
  lateinit var provisionRequestRunner: ProvisionRequestRunner
  @Autowired
  lateinit var bindingRequestRunner: BindingRequestRunner


  init {
    describe("Requests should contain header X-Broker-API-Version: 2.13") {

      wireAndUnwire()

      val catalogRequestRunner = CatalogRequestRunner(configuration)

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