package de.evoila.osb.checker.tests

import com.greghaskins.spectrum.Spectrum.*
import de.evoila.osb.checker.request.BindingRequestRunner
import de.evoila.osb.checker.request.CatalogRequestRunner
import de.evoila.osb.checker.request.ProvisionRequestRunner
import de.evoila.osb.checker.request.bodies.ProvisionBody.ValidProvisioning
import de.evoila.osb.checker.request.bodies.RequestBody.ValidBinding
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class BindingTest : TestBase() {

  @Autowired
  lateinit var catalogRequestRunner: CatalogRequestRunner
  @Autowired
  lateinit var provisionRequestRunner: ProvisionRequestRunner
  @Autowired
  lateinit var bindingRequestRunner: BindingRequestRunner


  init {

    describe("PUT /v2/service_instance/:instance_id/service_bindings/:binding_id") {

      beforeAll {
        wireAndUnwire()
      }

      it("should accept a valid binding request for each plan ID and delete the binding and instance afterwards.") {

        val catalog = catalogRequestRunner.correctRequest()

        catalog.services.parallelStream().forEach { service ->
          service.plans.parallelStream().forEach { plan ->

            log.info("Testing Service with ID ${service.id} and Plan with ID ${plan.id}")

            val instanceId = UUID.randomUUID().toString()
            val bindingId = UUID.randomUUID().toString()

            val provision = ValidProvisioning(service, plan)
            val binding = ValidBinding(service.id, plan.id)

            provisionRequestRunner.runPutProvisionRequestAsync(instanceId, provision, 202)

            assert(provisionRequestRunner.waitForFinish(instanceId) == "succeeded")

            bindingRequestRunner.runPutBindingRequest(binding, 201, instanceId, bindingId)
            bindingRequestRunner.runDeleteBindingRequest(binding.service_id, binding.plan_id, 200, instanceId, bindingId)

            provisionRequestRunner.runDeleteProvisionRequestAsync(instanceId, service.id, plan.id)
          }
        }
      }

      describe("Binding syntax") {

        wireAndUnwire()

        val catalogRequestRunner = CatalogRequestRunner()
        val catalog = catalogRequestRunner.correctRequest()

        val service = catalog.services.first()
        val plan = service.plans.first()
        val instanceId = UUID.randomUUID().toString()
        val bindingId = UUID.randomUUID().toString()

        val provision = ValidProvisioning(service, plan)
        val binding = ValidBinding(service.id, plan.id)

        provisionRequestRunner.runPutProvisionRequestAsync(instanceId, provision, 202)

        assert(provisionRequestRunner.waitForFinish(instanceId) == "succeeded")

        it("PUT - should reject if missing service_id") {
          val missingServiceID = ValidBinding(
              service_id = null,
              plan_id = binding.plan_id
          )
          bindingRequestRunner.runPutBindingRequest(missingServiceID, 400, instanceId, bindingId)
        }
        it("PUT - should reject if missing plan_id") {
          val missingServiceID = ValidBinding(
              service_id = binding.service_id,
              plan_id = null
          )
          bindingRequestRunner.runPutBindingRequest(missingServiceID, 400, instanceId, bindingId)
        }
        it("DELETE - should reject if missing service_id") {
          bindingRequestRunner.runDeleteBindingRequest(null, binding.plan_id, 400, instanceId, bindingId)
        }
        it("DELETE - should reject if missing plan_id") {
          bindingRequestRunner.runDeleteBindingRequest(binding.service_id, null, 400, instanceId, bindingId)
        }

        provisionRequestRunner.runDeleteProvisionRequestAsync(instanceId, service.id, plan.id)
      }
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(BindingTest::class.java)
  }
}
