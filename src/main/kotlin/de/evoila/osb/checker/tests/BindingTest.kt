package de.evoila.osb.checker.tests

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.Spectrum.describe
import com.greghaskins.spectrum.Spectrum.it
import de.evoila.osb.checker.Application
import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.request.BindingRequestRunner
import de.evoila.osb.checker.request.CatalogRequestRunner
import de.evoila.osb.checker.request.ProvisionRequestRunner
import de.evoila.osb.checker.request.bodies.ProvisionBody.*
import de.evoila.osb.checker.request.bodies.RequestBody.ValidBinding
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest(classes = [Application::class])
@RunWith(Spectrum::class)
class BindingTest : TestBase() {

  init {
    val catalogRequestRunner = CatalogRequestRunner(Configuration.token)
    val catalog = catalogRequestRunner.correctRequest()

    describe("PUT /v2/service_instance/:instance_id/service_bindings/:binding_id") {
      it("should accept a valid binding request for each plan ID and delete the binding and instance afterwards.") {

        catalog.services.parallelStream().forEach { service ->
          service.plans.parallelStream().forEach { plan ->

            log.info("Testing Service with ID ${service.id} and Plan with ID ${plan.id}")

            val instanceId = UUID.randomUUID().toString()
            val bindingId = UUID.randomUUID().toString()
            val provisionRequestRunner = ProvisionRequestRunner(instanceId)
            val bindingRequestRunner = BindingRequestRunner(instanceId, bindingId)
            val provision = ValidProvisioning(service, plan)
            val binding = ValidBinding(service.id, plan.id)

            provisionRequestRunner.runPutProvisionRequestAsync(provision, 202)

            assert(provisionRequestRunner.waitForFinish() == "succeeded")

            bindingRequestRunner.runPutBindingRequest(binding, 201)
            bindingRequestRunner.runDeleteBindingRequest(binding.service_id, binding.plan_id, 200)

            provisionRequestRunner.runDeleteProvisionRequestAsync(service.id, plan.id)
          }
        }
      }

      describe("Binding syntax") {

        val service = catalog.services.first()
        val plan = service.plans.first()
        val instanceId = UUID.randomUUID().toString()
        val bindingId = UUID.randomUUID().toString()
        val provisionRequestRunner = ProvisionRequestRunner(instanceId)
        val bindingRequestRunner = BindingRequestRunner(instanceId, bindingId)

        val provision = ValidProvisioning(service, plan)
        val binding = ValidBinding(service.id, plan.id)

        provisionRequestRunner.runPutProvisionRequestAsync(provision, 202)

        assert(provisionRequestRunner.waitForFinish() == "succeeded")

        it("PUT - should reject if missing service_id") {
          val missingServiceID = ValidBinding(
              service_id = null,
              plan_id = binding.plan_id
          )
          bindingRequestRunner.runPutBindingRequest(missingServiceID, 400)
        }
        it("PUT - should reject if missing plan_id") {
          val missingServiceID = ValidBinding(
              service_id = binding.service_id,
              plan_id = null
          )
          bindingRequestRunner.runPutBindingRequest(missingServiceID, 400)
        }
        it("DELETE - should reject if missing service_id") {
          bindingRequestRunner.runDeleteBindingRequest(null, binding.plan_id, 400)
        }
        it("DELETE - should reject if missing plan_id") {
          bindingRequestRunner.runDeleteBindingRequest(binding.service_id, null, 400)
        }

        provisionRequestRunner.runDeleteProvisionRequestAsync(service.id, plan.id)
      }
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(BindingTest::class.java)
  }
}
