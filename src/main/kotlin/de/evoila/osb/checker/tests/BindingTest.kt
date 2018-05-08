package de.evoila.osb.checker.tests

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.Spectrum.describe
import com.greghaskins.spectrum.Spectrum.it
import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.request.BindingRequestRunner.runDeleteBindingRequest
import de.evoila.osb.checker.request.BindingRequestRunner.runPutBindingRequest
import de.evoila.osb.checker.request.CatalogRequestRunner
import de.evoila.osb.checker.request.ProvisionRequestRunner.runPutProvisionRequestAsync
import de.evoila.osb.checker.request.ProvisionRequestRunner.waitForFinish
import de.evoila.osb.checker.request.bodies.ProvisionBody.*
import de.evoila.osb.checker.request.bodies.RequestBody.ValidBinding
import org.junit.runner.RunWith

@RunWith(Spectrum::class)
class BindingTest : TestBase() {
  init {

    val cat = CatalogRequestRunner.correctRequest(Configuration.token)
    val provistion = ValidProvisioning(cat)
    val binding = ValidBinding(cat)

    describe("PUT /v2/service_instance/:instance_id/service_bindings/:binding_id") {

      runPutProvisionRequestAsync(provistion, 202)
      assert(waitForFinish() == "succeeded")


      it("should reject if missing service_id") {
        val missingServiceID = ValidBinding(
            service_id = null,
            plan_id = binding.plan_id
        )
        runPutBindingRequest(missingServiceID, 400)
      }
      it("should reject if missing plan_id") {
        val missingServiceID = ValidBinding(
            service_id = binding.service_id,
            plan_id = null
        )
        runPutBindingRequest(missingServiceID, 400)
      }
      describe("NEW") {
        it("should accept a valid binding request") {
          runPutBindingRequest(binding, 201)
        }
      }

      describe("DELETE /v2/service_instance/:instance_id/service_bindings/:binding_id") {
        describe("BINDING - delete syntax") {
          it("should reject if missing service_id") {
            runDeleteBindingRequest(null, binding.plan_id, 400)
          }
          it("should reject if missing plan_id") {
            runDeleteBindingRequest(binding.service_id, null, 400)
          }
          it("should accept a valid binding deletion request") {
            runDeleteBindingRequest(binding.service_id, binding.plan_id, 200)
          }
        }
      }
    }
  }
}