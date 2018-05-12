package de.evoila.osb.checker.tests.contract

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.Spectrum.describe
import com.greghaskins.spectrum.Spectrum.it
import de.evoila.osb.checker.Application
import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.request.BindingRequestRunner
import de.evoila.osb.checker.request.CatalogRequestRunner
import de.evoila.osb.checker.request.ProvisionRequestRunner
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [Application::class])
@RunWith(Spectrum::class)
class AuthenticationTest(
) {
  init {

    describe("Service Broker should reject unauthorized access.") {
      val catalogRequestRunner = CatalogRequestRunner(Configuration.token)
      val provisionRequestRunner = ProvisionRequestRunner(Configuration.NOT_AN_ID)
      val bindingRequestRunner = BindingRequestRunner(Configuration.NOT_AN_ID, Configuration.NOT_AN_ID)

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