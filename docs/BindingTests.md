## Binding Tests

* Valid binding attempts 
    - Calls the catalog to set up a valid provision requests or uses a custom catalog, from the yml provided by the operator.
    - For each plan_id defined in the catalog the application will call:
      `curl http://username:password@broker-url/v2/service_instances/:instance_id?accepts_incomplete=true -d `
    ```json
    {
     "service_id": "service-id-here",
    "plan_id": "plan-id-here",
    "organization_guid": "org-guid-here",
    "space_guid": "space-guid-here"
    }
    ```
    ` -X PUT -H "X-Broker-API-Version: api-version-here" -H "Content-Type: application/json"` and validate if it returns 200 or 202 and that the response body matches the specification.

    - in case of 202 the test continues polling by calling  `curl http://username:password@broker-url/v2/service_instances/:instance_id/last_operation" ` 
    unit the service broker returns 200 and that the response body matches the specification. Note: Depending on the service broker this may take a while.

    - If a provision has finished and the service is bindable the test continues with the 2 following calls:    
        - `curl http://username:password@broker-url/v2/service_instances/:instance_id/service_bindings/:binding_id -d '`
        ```json
        {
      "service_id": "service-id-here",
      "plan_id": "plan-id-here",
      "bind_resource": {
        "app_guid": "app-guid-here"
           }
        }
        ```
        `' -X PUT -H "X-Broker-API-Version: api-version-here"` and validate if it returns 201 and that the response body matches the specification. 
        - `curl 'http://username:password@broker-url/v2/service_instances/:instance_id/service_bindings/:binding_id?service_id=service-id-here&plan_id=plan-id-here' -X DELETE -H "X-Broker-API-Version: api-version-here"`
        and validate if the service broker returns 200.
    
    - The provisioned service will now be deleted by calling `curl 'http://username:password@broker-url/v2/service_instances/:instance_id?accepts_incomplete=true&service_id=service-id-here&plan_id=plan-id-here' -X DELETE -H "X-Broker-API-Version: api-version-here"`
    The test checks if the response is 200 or 202.
    
    - in case of 202 the test continues polling by calling  `curl http://username:password@broker-url/v2/service_instances/:instance_id/last_operation" ` 
    unit the service broker returns 410. Note: Depending on the service broker this may take a while.
    
* Invalid binding attempts
    - This tests will only run if the service is bindable.
    - calls the catalog and makes a runs a single provision
    - runs invalid binding and unbinding attempts and validates of the service broker returns 400.
    
Look [here](https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#binding) for more information about binding and unbinding.

### Example output
A Binding Test output with one deployed service.
```
╷
└─ JUnit Jupiter ✔
   └─ Binding Tests ✔
      ├─ Run asynchronous invalid PUT and DELETE binding attempts. ✔
      │  ├─ Creating Service Instance, fetching it. ✔
      │  │  ├─ Running valid PUT provision with instanceId 6e2f0ded-1d20-4d0e-b16e-548a52440251 for service 'base-sql-service-dev-managed' and plan 's' ✔
      │  │  ├─ Running valid PUT provision with same attributes again. Expecting Status 200. ✔
      │  │  └─ Running valid PUT provision with different attributes again. Expecting Status 409. ✔
      │  ├─ Run sync and invalid bindings attempts ✔
      │  │  ├─ should return status code 4XX when tying to fetch a non existing binding ✔
      │  │  ├─ should handle sync requests correctly ✔
      │  │  │  ├─ Sync PUT binding request ✔
      │  │  │  └─ Sync DELETE binding request ✔
      │  │  ├─ PUT should reject if missing service_id ✔
      │  │  ├─ DELETE should reject if missing service_id ✔
      │  │  ├─ PUT should reject if missing plan_id ✔
      │  │  └─ DELETE should reject if missing plan_id ✔
      │  └─ Deleting provision ✔
      │     ├─ DELETE provision and if the service broker is async polling afterwards ✔
      │     └─ Running valid DELETE provision with same parameters again. Expecting Status 410. ✔
      └─ Valid Provision and Bindings. ✔
         └─ Running a valid provision and if the service is bindable a valid binding. Deleting both afterwards. In case of a async service broker poll afterwards. ✔
            ├─ Creating Service Instance, fetching it. ✔
            │  ├─ Running valid PUT provision with instanceId 158bfb75-bba2-4ea9-aeb5-3d7fcc6a8c70 for service 'base-sql-service-dev-managed' and plan 's' ✔
            │  ├─ Running valid PUT provision with same attributes again. Expecting Status 200. ✔
            │  └─ Running valid PUT provision with different attributes again. Expecting Status 409. ✔
            ├─ Running PUT binding and DELETE binding afterwards ✔
            │  ├─ Running valid PUT binding with bindingId 73f9151a-00df-474d-b464-31c83e908024 ✔
            │  ├─ Running PUT binding with same attribute again. Expecting StatusCode 200. ✔
            │  ├─ Running PUT binding with different attribute again. Expecting StatusCode 409. ✔
            │  ├─ Running GET for retrievable service binding and expecting StatusCode: 200 ✔
            │  └─ Deleting binding with bindingId 73f9151a-00df-474d-b464-31c83e908024 ✔
            └─ Deleting provision ✔
               ├─ DELETE provision and if the service broker is async polling afterwards ✔
               └─ Running valid DELETE provision with same parameters again. Expecting Status 410. ✔

Test run finished after 8563 ms
[        12 containers found      ]
[         0 containers skipped    ]
[        12 containers started    ]
[         0 containers aborted    ]
[        12 containers successful ]
[         0 containers failed     ]
[        22 tests found           ]
[         0 tests skipped         ]
[        22 tests started         ]
[         0 tests aborted         ]
[        18 tests successful      ]
[         4 tests failed          ]
```
