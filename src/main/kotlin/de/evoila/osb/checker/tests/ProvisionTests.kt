package de.evoila.osb.checker.tests


import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.Spectrum.*
import de.evoila.osb.checker.Application
import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.request.CatalogRequestRunner
import de.evoila.osb.checker.request.bodies.ProvisionBody
import de.evoila.osb.checker.request.bodies.ProvisionBody.*
import de.evoila.osb.checker.request.bodies.RequestBody.Invalid
import de.evoila.osb.checker.response.Plan
import de.evoila.osb.checker.response.Service
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@SpringBootTest(classes = [Application::class])
@RunWith(Spectrum::class)
class ProvisionTests : TestBase() {

  @Autowired
  lateinit var catalogRequestRunner: CatalogRequestRunner

  val usedIds: MutableMap<String, Provision> = Collections.synchronizedMap(hashMapOf<String, Provision>())

  init {

    beforeAll {
      wireAndUnwire()
    }

    afterAll {
      cleanUp(usedIds)
    }

    describe("make a provision request and start polling if it's a async service broker. Afterwards the provision should be deleted") {

      val catalog = catalogRequestRunner.correctRequest()

      var isAsync = true

      it("should handle a sync Put and Delete request correctly") {

        val instanceId = UUID.randomUUID().toString()

        val service = catalog.services.first()
        val plan = service.plans.first()

        val provisionRequestBody = ValidProvisioning(service, plan)
        usedIds[instanceId] = Provision(
            serviceID = service.id,
            planId = plan.id
        )

        val statusCodePut = provisionRequestRunner.runPutProvisionRequestSync(instanceId, provisionRequestBody)
        assertTrue("Should return  201 in case of a sync Service Broker or 422 if it's async but it was $statusCodePut.")
        { statusCodePut in listOf(201, 422) }

        isAsync = statusCodePut != 201

        val statusCodeDelete = provisionRequestRunner.runDeleteProvisionRequestSync(instanceId, provisionRequestBody.service_id, provisionRequestBody.plan_id)
        assertTrue("Delete should return 200, 201, 410, 422 but was $statusCodeDelete") { statusCodeDelete in listOf(200, 201, 410, 422) }

      }

      it("should accept a valid async provision request for each service and plan -id in the catalog." +
          "In case of async Polling should correctly." +
          "If a second instance with the same ID is being created. 409 should be returned" +
          "Afterwards the Instance will be deleted") {

        val services = if (Configuration.maxServices < catalog.services.size) {
          catalog.services.subList(0, Configuration.maxServices)
        } else {
          catalog.services
        }

        services.forEach { service ->
          service.plans.parallelStream().forEach { plan ->

            testProvision(service, plan, isAsync)
          }
        }
      }
    }

    describe("Testing provisioning Syntax") {

      val catalog = catalogRequestRunner.correctRequest()

      val instanceId = UUID.randomUUID().toString()


      val service = catalog.services.first()
      val plan = service.plans.first()
      val provisionRequestBody = ValidProvisioning(service, plan)

      usedIds[instanceId] = Provision(
          serviceID = service.id,
          planId = plan.id
      )

      it("PUT - should reject if missing service_id") {
        val missingServiceID = ValidProvisioning(service_id = null, plan_id = provisionRequestBody.plan_id)
        provisionRequestRunner.runPutProvisionRequestAsync(instanceId, missingServiceID, 400)
      }

      it("PUT - should reject if missing service_id field") {
        val missingServiceID = NoServiceFieldProvisioning(service)
        provisionRequestRunner.runPutProvisionRequestAsync(instanceId, missingServiceID, 400)
      }

      it("PUT - should reject if missing plan_id field") {
        val missingPlanId = ProvisionBody.NoPlanFieldProvisioning(plan)
        provisionRequestRunner.runPutProvisionRequestAsync(instanceId, missingPlanId, 400)
      }

      it("PUT - should reject if missing plan_id") {
        val missingPlanId = ValidProvisioning(
            service_id = provisionRequestBody.service_id,
            plan_id = null
        )
        provisionRequestRunner.runPutProvisionRequestAsync(instanceId, missingPlanId, 400)
      }

      it("PUT - should reject if request payload is missing organization_guid field") {
        val missingOrgGuid = NoOrgFieldProvisioning(
            service, plan
        )
        provisionRequestRunner.runPutProvisionRequestAsync(instanceId, missingOrgGuid, 400)
      }


      it("PUT - should reject if request payload is missing organization_guid") {
        val missingOrgGuid = ValidProvisioning(
            service_id = provisionRequestBody.service_id,
            plan_id = provisionRequestBody.plan_id,
            organization_guid = null
        )
        provisionRequestRunner.runPutProvisionRequestAsync(instanceId, missingOrgGuid, 400)
      }

      it("PUT - should reject if request payload is missing space_guid field") {
        val missingSpaceGuid = NoSpaceFieldProvisioning(
            service, plan
        )
        provisionRequestRunner.runPutProvisionRequestAsync(instanceId, missingSpaceGuid, 400)
      }

      it("PUT - should reject if request payload is missing space_guid") {
        val missingSpaceGuid = ValidProvisioning(
            service_id = provisionRequestBody.service_id,
            plan_id = provisionRequestBody.plan_id,
            space_guid = null
        )
        provisionRequestRunner.runPutProvisionRequestAsync(instanceId, missingSpaceGuid, 400)
      }

      it("PUT - should reject if service_id is invalid") {
        val invalidServiceId = ValidProvisioning(
            service_id = "Invalid",
            plan_id = provisionRequestBody.plan_id
        )
        provisionRequestRunner.runPutProvisionRequestAsync(instanceId, invalidServiceId, 400)
      }

      it("PUT - should reject if plan_id is invalid") {
        val invalidPlanId = ValidProvisioning(
            service_id = provisionRequestBody.service_id,
            plan_id = "Invalid"
        )
        provisionRequestRunner.runPutProvisionRequestAsync(instanceId, invalidPlanId, 400)
      }

      it("PUT - should reject if parameters are not following schema") {
        provisionRequestRunner.runPutProvisionRequestAsync(instanceId, Invalid(), 400)
      }

      it("DELETE - should reject if missing service_id") {

        val statusCode = provisionRequestRunner.runDeleteProvisionRequestAsync(
            instanceId,
            null,
            provisionRequestBody.plan_id)
        assertEquals(400, statusCode, "Status Code should haveen been 400 but was $statusCode")
      }

      it("DELETE - should reject if missing plan_id") {

        val statusCode = provisionRequestRunner.runDeleteProvisionRequestAsync(
            instanceId,
            provisionRequestBody.service_id,
            null)
        assertEquals(400, statusCode, "Status Code should haveen been 400 but was $statusCode")
      }
    }
  }

  private fun testProvision(service: Service, plan: Plan, isAsync: Boolean) {

    val instanceId = UUID.randomUUID().toString()

    usedIds[instanceId] = Provision(
        serviceID = service.id,
        planId = plan.id
    )

    val provisionRequestBody = ValidProvisioning(service, plan)

    provisionRequestRunner.runPutProvisionRequestAsync(instanceId, provisionRequestBody, 202)

    if (isAsync) {
      provisionRequestRunner.runGetLastOperation(instanceId, 200)
      provisionRequestRunner.waitForFinish(instanceId)
    }
    provisionRequestRunner.runPutProvisionRequestAsync(instanceId, provisionRequestBody, 409)

    if (plan.plan_updatable) {
      provisionRequestRunner.runPatchProvisionRequest(instanceId, provisionRequestBody, 200)
    }

    val statusCode = provisionRequestRunner.runDeleteProvisionRequestAsync(
        instanceId,
        provisionRequestBody.service_id,
        provisionRequestBody.plan_id)

    assertTrue("statusCode should be 200 or 202 but was $statusCode.") { statusCode in listOf(200, 202) }
  }
}
