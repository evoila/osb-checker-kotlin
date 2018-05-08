package de.evoila.osb.checker.tests


import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.Spectrum.describe
import com.greghaskins.spectrum.Spectrum.it
import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.request.CatalogRequestRunner
import de.evoila.osb.checker.request.ProvisionRequestRunner.runDeleteProvisionRequestAsync
import de.evoila.osb.checker.request.ProvisionRequestRunner.runDeleteProvisionRequestSync
import de.evoila.osb.checker.request.ProvisionRequestRunner.runGetLastOperation
import de.evoila.osb.checker.request.ProvisionRequestRunner.runPutProvisionRequestAsync
import de.evoila.osb.checker.request.ProvisionRequestRunner.runPutProvisionRequestSync
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

    catalog.services.parallelStream().forEach { service ->
      service.plans.parallelStream().forEach { plan ->

        val provisionRequestBody = ValidProvisioning(service, plan)

        describe("make a provision request and start polling if it's a async service broker") {

          var isAsync = true

          it("should handle a sync request correctly") {
            val statusCode = runPutProvisionRequestSync(provisionRequestBody)
            assertTrue("Should return  201 in case of a sync Service Broker or 422 if it's async.")
            { statusCode in listOf(201, 422) }

            if (statusCode == 201) {
              val statusCode = runDeleteProvisionRequestSync(provisionRequestBody.service_id, provisionRequestBody.plan_id)
              assertTrue("Should delete if not async and return 200") { statusCode == 200 }
              isAsync = false
            }
          }

          if (isAsync) {

            it("should accept a valid async provision request") {
              runPutProvisionRequestAsync(provisionRequestBody, 202)
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
              runPutProvisionRequestAsync(provisionRequestBody, 409)
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

                val statusCode = runDeleteProvisionRequestAsync(
                    null,
                    provisionRequestBody.plan_id)
                assertEquals(400, statusCode, "Status Code should haveen been 400 but was $statusCode")
              }
              it("should reject if missing plan_id") {

                val statusCode = runDeleteProvisionRequestAsync(
                    provisionRequestBody.service_id,
                    null)
                assertEquals(400, statusCode, "Status Code should haveen been 400 but was $statusCode")
              }
              it("should accept a valid service deletion request") {

                val statusCode = runDeleteProvisionRequestAsync(
                    provisionRequestBody.service_id,
                    provisionRequestBody.plan_id)

                assertTrue("statusCode should be 200 or 202 but was $statusCode.") { statusCode in listOf(200, 202) }
              }
            }
          }

          describe("Testing provisioning Syntax") {

            it("should reject if missing service_id") {
              val missingServiceID = ValidProvisioning(service_id = null, plan_id = provisionRequestBody.plan_id)
              runPutProvisionRequestAsync(missingServiceID, 400)
            }

            it("should reject if missing service_id field") {
              val missingServiceID = NoServiceFieldProvisioning(service)
              runPutProvisionRequestAsync(missingServiceID, 400)
            }

            it("should reject if missing plan_id field") {
              val missingPlanId = ProvisionBody.NoPlanFieldProvisioning(plan)
              runPutProvisionRequestAsync(missingPlanId, 400)
            }

            it("should reject if missing plan_id") {
              val missingPlanId = ValidProvisioning(
                  service_id = provisionRequestBody.service_id,
                  plan_id = null
              )
              runPutProvisionRequestAsync(missingPlanId, 400)
            }

            it("should reject if request payload is missing organization_guid field") {
              val missingOrgGuid = NoOrgFieldProvisioning(
                  service, plan
              )
              runPutProvisionRequestAsync(missingOrgGuid, 400)
            }


            it("should reject if request payload is missing organization_guid") {
              val missingOrgGuid = ValidProvisioning(
                  service_id = provisionRequestBody.service_id,
                  plan_id = provisionRequestBody.plan_id,
                  organization_guid = null
              )
              runPutProvisionRequestAsync(missingOrgGuid, 400)
            }

            it("should reject if request payload is missing space_guid field") {
              val missingSpaceGuid = NoSpaceFieldProvisioning(
                  service, plan
              )
              runPutProvisionRequestAsync(missingSpaceGuid, 400)
            }


            it("should reject if request payload is missing space_guid") {
              val missingSpaceGuid = ValidProvisioning(
                  service_id = provisionRequestBody.service_id,
                  plan_id = provisionRequestBody.plan_id,
                  space_guid = null
              )
              runPutProvisionRequestAsync(missingSpaceGuid, 400)
            }

            it("should reject if service_id is invalid") {
              val invalidServiceId = ValidProvisioning(
                  service_id = "Invalid",
                  plan_id = provisionRequestBody.plan_id
              )
              runPutProvisionRequestAsync(invalidServiceId, 400)
            }
            it("should reject if plan_id is invalid") {
              val invalidPlanId = ValidProvisioning(
                  service_id = provisionRequestBody.service_id,
                  plan_id = "Invalid"
              )
              runPutProvisionRequestAsync(invalidPlanId, 400)
            }
            it("should reject if parameters are not following schema") {
              runPutProvisionRequestAsync(Invalid(), 400)
            }
          }
        }
      }
    }
  }
}