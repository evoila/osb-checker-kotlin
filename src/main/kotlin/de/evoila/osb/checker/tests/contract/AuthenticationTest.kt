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
class AuthenticationTest : TestBase() {

  @Autowired
  lateinit var bindingRequestRunner: BindingRequestRunner

  init {
    describe("Service Broker should reject unauthorized access.") {
      wireAndUnwire()

      val catalogRequestRunner = CatalogRequestRunner()

      it("GET - v2/catalog should reject with 401") {
        catalogRequestRunner.noAuth()
      }

      it("PUT - v2/service_instance/instance_id should reject with 401") {
        provisionRequestRunner.putNoAuth()
      }

      it("GET - v2/service_instance/instance_id/last_operation should reject with 401") {
        provisionRequestRunner.lastOpNoAuth()
      }

      it("DELETE - v2/service_instance/instance_id should reject with 401") {
        provisionRequestRunner.deleteNoAuth()
      }

      it("PUT - v2/service_instance/instance_id/service_binding/binding_id  should reject with 401") {
        bindingRequestRunner.putNoAuth()
      }

      it("DELETE - v2/service_instance/instance_id/service_binding/binding_id  should reject with 401") {
        bindingRequestRunner.deleteNoAuth()
      }
    }
  }
}