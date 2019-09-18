## Provision Tests

- Calls the catalog to set up a valid provision requests or uses a custom catalog, from the yml provided by the operator.
- For each plan_id defined in the catalog the application will call:
  `curl http://username:password@broker-url/v2/service_instances/:instance_id -d `
```json
{
  "service_id": "service-id-here",
  "plan_id": "plan-id-here",
  "organization_guid": "org-guid-here",
  "space_guid": "space-guid-here"
}
```
` -X PUT -H "X-Broker-API-Version: api-version-here" -H "Content-Type: application/json"`
- validate that the service broker returns 201 in case of a sync service broker or 422 in case of a async service broker.
- Now it calls `curl http://username:password@broker-url/v2/service_instances/:instance_id?service_id=service-id-here&plan_id=plan-id-here -d  DELETE -H "X-Broker-API-Version: api-version-here" -H "Content-Type: application/json"` 
 and validate if the service broker returns 200 in case of a sync service broker or 422 in case of a async service broker.
- validates the broker returns 400 when being given faulty provision requests.

- Note: This test can only run successfully if the Service Broker returns a valid catalog or a correct custom catalog has been set im the .yml file.

Look [here](https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#provisioning) (provisioning)
and [here](https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#deprovisioning) (deprovisioning) for more information.