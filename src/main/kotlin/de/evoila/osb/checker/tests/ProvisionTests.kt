package de.evoila.osb.checker.tests


import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.Spectrum.describe
import com.greghaskins.spectrum.Spectrum.it
import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.request.CatalogRequestRunner
import de.evoila.osb.checker.request.ProvisionRequestRunner.runDeleteProvisionRequest
import de.evoila.osb.checker.request.ProvisionRequestRunner.runGetLastOperation
import de.evoila.osb.checker.request.ProvisionRequestRunner.runPutProvisionRequest
import de.evoila.osb.checker.request.ProvisionRequestRunner.waitForFinish
import de.evoila.osb.checker.request.bodies.ProvisionBody
import de.evoila.osb.checker.request.bodies.ProvisionBody.*
import de.evoila.osb.checker.request.bodies.RequestBody.Invalid
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(Spectrum::class)
class ProvisionTests : TestBase() {
  init {

    val catalog = CatalogRequestRunner.correctRequest(Configuration.token)
    val provisionRequestBody = ValidProvisioning(catalog)

    describe("PROVISION - new") {
      it("should accept a valid async provision request") {
        runPutProvisionRequest(provisionRequestBody, 202)
      }
      describe("PROVISION - query after new") {
        it("should return last operation status") {
          runGetLastOperation(200)
          waitForFinish()
        }
      }
    }
    describe("PROVISION - conflict") {
      it("should return conflict when instance Id exists with different properties") {
        runPutProvisionRequest(provisionRequestBody, 409)
      }
    }

    /*
    describe("PATCH /v2/service_instance/:instance_id") {
      it("should reject if missing service_id") {

        val missingServiceId = RequestBody.Update(
            null
        )
        runPatchProvisionRequest(missingServiceId, 400)
      }

      it("should accept a valid update request") {
        //TODO Build a proper Test here
      }
    }
*/

    describe("DELETE /v2/service_instance/:instance_id") {
      describe("DELETE") {
        it("should reject if missing service_id") {

          val statusCode = runDeleteProvisionRequest(
              null,
              provisionRequestBody.plan_id)
          assertEquals(400, statusCode, "Status Code should haveen been 400 but was $statusCode")
        }
        it("should reject if missing plan_id") {

          val statusCode = runDeleteProvisionRequest(
              provisionRequestBody.service_id,
              null)
          assertEquals(400, statusCode, "Status Code should haveen been 400 but was $statusCode")
        }
        it("should accept a valid service deletion request") {

          val statusCode = runDeleteProvisionRequest(
              provisionRequestBody.service_id,
              provisionRequestBody.plan_id)

          assertTrue("statusCode should be 200 or 202 but was.") { statusCode in listOf(200, 202) }
        }
      }
    }

    describe("Testing provisioning Syntax") {

      it("should reject if missing service_id") {
        val missingServiceID = ValidProvisioning(
            service_id = null,
            plan_id = provisionRequestBody.plan_id
        )
        runPutProvisionRequest(
            missingServiceID,
            400)
      }
      it("should reject if missing service_id field") {
        val missingServiceID = NoServiceFieldProvisioning(catalog)
        runPutProvisionRequest(
            missingServiceID,
            400)
      }

      it("should reject if missing plan_id field") {
        val missingPlanId = ProvisionBody.NpPlanFieldProvisioning(
            catalog
        )
        runPutProvisionRequest(missingPlanId, 400)
      }

      it("should reject if missing plan_id") {
        val missingPlanId = ValidProvisioning(
            service_id = provisionRequestBody.service_id,
            plan_id = null
        )
        runPutProvisionRequest(missingPlanId, 400)
      }

      it("should reject if request payload is missing organization_guid field") {
        val missingOrgGuid = NoOrgFieldProvisioning(
            catalog
        )
        runPutProvisionRequest(missingOrgGuid, 400)
      }


      it("should reject if request payload is missing organization_guid") {
        val missingOrgGuid = ValidProvisioning(
            service_id = provisionRequestBody.service_id,
            plan_id = provisionRequestBody.plan_id,
            organization_guid = null
        )
        runPutProvisionRequest(missingOrgGuid, 400)
      }

      it("should reject if request payload is missing space_guid field") {
        val missingSpaceGuid = NoSpaceFieldProvisioning(
            catalog
        )
        runPutProvisionRequest(missingSpaceGuid, 400)
      }


      it("should reject if request payload is missing space_guid") {
        val missingSpaceGuid = ValidProvisioning(
            service_id = provisionRequestBody.service_id,
            plan_id = provisionRequestBody.plan_id,
            space_guid = null
        )
        runPutProvisionRequest(missingSpaceGuid, 400)
      }

      it("should reject if service_id is invalid") {
        val invalidServiceId = ValidProvisioning(
            service_id = "Invalid",
            plan_id = provisionRequestBody.plan_id
        )
        runPutProvisionRequest(invalidServiceId, 400)
      }
      it("should reject if plan_id is invalid") {
        val invalidPlanId = ValidProvisioning(
            service_id = provisionRequestBody.service_id,
            plan_id = "Invalid"
        )
        runPutProvisionRequest(invalidPlanId, 400)
      }
      it("should reject if parameters are not following schema") {
        runPutProvisionRequest(Invalid(), 400)
      }
    }
  }
}