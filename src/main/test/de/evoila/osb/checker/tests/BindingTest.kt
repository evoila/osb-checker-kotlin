package de.evoila.osb.checker.tests

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.Spectrum.describe
import com.greghaskins.spectrum.Spectrum.it
import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.request.BindingRequestRunner.runDeleteBinfingRequest
import de.evoila.osb.checker.request.BindingRequestRunner.runPutBinfingRequest
import de.evoila.osb.checker.request.CatalogRequestRunner
import de.evoila.osb.checker.request.ProvisionRequestRunner.runDeleteProvisionRequest
import de.evoila.osb.checker.request.ProvisionRequestRunner.runPutProvisionRequest
import de.evoila.osb.checker.request.ProvisionRequestRunner.waitForFinish
import de.evoila.osb.checker.request.bodies.RequestBody.ValidBinding
import de.evoila.osb.checker.request.bodies.RequestBody.ValidProvisioning
import io.restassured.RestAssured
import org.junit.Assert
import org.junit.runner.RunWith

@RunWith(Spectrum::class)
class BindingTest {

  init {

    RestAssured.baseURI = Configuration.url
    RestAssured.port = RestAssured.port
    RestAssured.authentication = RestAssured.basic("admin", "cloudfoundry")


    val cat = CatalogRequestRunner.correctRequest(Configuration.token)
    val provistion = ValidProvisioning(cat)
    val binding = ValidBinding(cat)


    describe("PUT /v2/service_instance/:instance_id/service_bindings/:binding_id") {

      runPutProvisionRequest(provistion, 202)
      assert(waitForFinish() == "succeeded")


      it("should reject if missing service_id") {
        val missingServiceID = ValidBinding(
            service_id = null,
            plan_id = binding.plan_id
        )
        runPutBinfingRequest(missingServiceID, 400)
      }
      it("should reject if missing plan_id") {
        val missingServiceID = ValidBinding(
            service_id = binding.service_id,
            plan_id = null
        )
        runPutBinfingRequest(missingServiceID, 400)
      }
      describe("NEW") {
        it("should accept a valid binding request") {
          runPutBinfingRequest(binding, 201)
        }
      }

      describe("DELETE /v2/service_instance/:instance_id/service_bindings/:binding_id") {
        describe("BINDING - delete syntax") {
          it("should reject if missing service_id") {
            runDeleteBinfingRequest(null, binding.plan_id, 400)
          }
          it("should reject if missing plan_id") {
            runDeleteBinfingRequest(binding.service_id, null, 400)
          }
          it("should accept a valid binding deletion request") {
            runDeleteBinfingRequest(binding.service_id, binding.plan_id, 200)
          }
        }
      }

      runDeleteProvisionRequest(provistion.service_id, provistion.plan_id, 202)
    }
  }
}