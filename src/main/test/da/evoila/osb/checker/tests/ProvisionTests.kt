package da.evoila.osb.checker.tests


import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.Spectrum.describe
import com.greghaskins.spectrum.Spectrum.it
import da.evoila.osb.checker.tests.Scenario.*
import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.request.CatalogRequestRunner
import de.evoila.osb.checker.request.ProvisionRequestRunner.runDeleteProvisionRequest
import de.evoila.osb.checker.request.ProvisionRequestRunner.runPatchProvisionRequest
import de.evoila.osb.checker.request.ProvisionRequestRunner.runPutProvisionRequest
import de.evoila.osb.checker.request.ProvisionRequestRunner.validLastOperationStatus
import de.evoila.osb.checker.request.bodies.ProvisionRequestBody
import de.evoila.osb.checker.request.bodies.ProvisionRequestBody.*
import io.restassured.RestAssured
import org.junit.runner.RunWith

@RunWith(Spectrum::class)
class ProvisionTests {
  init {
    RestAssured.baseURI = Configuration.url
    RestAssured.port = RestAssured.port
    RestAssured.authentication = RestAssured.basic("admin", "cloudfoundry")

    val cat = CatalogRequestRunner.correctRequest(Configuration.token)
    val provisionRequestBody = Valid(cat)

    listOf(NEW, CONFLICT).forEach { scenario ->

      describe("Testing provisioning $scenario") {
        it("should reject if missing service_id") {
          val missingServiceID = Valid(
              service_id = null,
              plan_id = provisionRequestBody.plan_id
          )
          runPutProvisionRequest(
              missingServiceID,
              400)
        }
        it("should reject if missing plan_id") {
          val missingPlanId = Valid(
              service_id = provisionRequestBody.service_id,
              plan_id = null
          )
          runPutProvisionRequest(missingPlanId, 400)
        }
        it("should reject if request payload is missing organization_guid") {
          val missingOrgGuid = Valid(
              service_id = provisionRequestBody.service_id,
              plan_id = provisionRequestBody.plan_id,
              organization_guid = null
          )
          runPutProvisionRequest(missingOrgGuid, 400)
        }
        it("should reject if request payload is missing space_guid") {
          val missingSpaceGuid = Valid(
              service_id = provisionRequestBody.service_id,
              plan_id = provisionRequestBody.plan_id,
              space_guid = null
          )
          runPutProvisionRequest(missingSpaceGuid, 400)

        }
        it("should reject if service_id is invalid") {
          val invalidServiceId = Valid(
              service_id = "Invalid",
              plan_id = provisionRequestBody.plan_id
          )
          runPutProvisionRequest(invalidServiceId, 400)
        }
        it("should reject if plan_id is invalid") {
          val invalidPlanId = Valid(
              service_id = provisionRequestBody.service_id,
              plan_id = "Invalid"
          )
          runPutProvisionRequest(invalidPlanId, 400)
        }
        it("should reject if parameters are not following schema") {
          runPutProvisionRequest(Invalid(), 400)
        }
        when (scenario) {
          NEW -> {
            describe("PROVISION - new") {
              it("should accept a valid provision request") {
                runPutProvisionRequest(provisionRequestBody, 202)
              }
              describe("PROVISION - query after new") {
                it("should return last operation status") {
                  validLastOperationStatus(provisionRequestBody)
                }
              }
            }
          }
          CONFLICT -> {
            describe("PROVISION - conflict") {
              it("should return conflict when instance Id exists with different properties") {
                runPutProvisionRequest(provisionRequestBody, 202)
                runPutProvisionRequest(provisionRequestBody, 409)
              }
            }
          }
        }

        describe("PATCH /v2/service_instance/:instance_id") {
          it("should reject if missing service_id") {

            val missingServiceId = ProvisionRequestBody.Update(
                null
            )
            runPatchProvisionRequest(missingServiceId, 400)
          }
        }

        describe("DELETE /v2/service_instance/:instance_id") {
          describe("DELETE") {
            it("should reject if missing service_id") {
              runDeleteProvisionRequest(
                  null,
                  provisionRequestBody.plan_id,
                  400)
            }
            it("should reject if missing plan_id") {

              runDeleteProvisionRequest(
                  provisionRequestBody.service_id,
                  null,
                  400)
            }
            it("should accept a valid service deletion request") {
              runDeleteProvisionRequest(
                  provisionRequestBody.service_id,
                  provisionRequestBody.plan_id,
                  202)
            }
          }
        }
      }
    }
  }
}

enum class Scenario {
  NEW,
  CONFLICT
}