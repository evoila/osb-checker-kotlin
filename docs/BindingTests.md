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
   └─ BindingJUnit5 ✔
      ├─ runInvalidBindingAttempts() ✔
      │  ├─ Provision and in case of a async service broker polling, for later binding ✔
      │  │  └─ Running Valid PUT provision with instanceId feaf198a-ec45-4c5b-a35b-77bd4b9c6f95 ✔
      │  ├─ Running invalid bindings ✔
      │  │  ├─ PUT should reject if missing service_id ✔
      │  │  ├─ DELETE should reject if missing service_id ✔
      │  │  ├─ PUT should reject if missing plan_id ✔
      │  │  └─ DELETE should reject if missing plan_id ✔
      │  └─ Deleting Provision ✔
      │     └─ DELETE provision and if the service broker is async polling afterwards ✔
      └─ runValidBindings() ✔
         └─ Running a valid provision if the service is bindable a valid binding. Deleting both afterwards. ✔
            ├─ Provision and in case of a async service broker polling, for later binding ✔
            │  └─ Running Valid PUT provision with instanceId b4206514-2915-4fea-b140-63d942aeb25e ✔
            ├─ Running PUT Binding and DELETE Binding afterwards ✔
            │  ├─ Running a valid binding with BindingId e882b7ba-47c6-40f2-89a0-0e08c6b342c5 60778 ms ✔
            │  └─ Deleting binding with bindingId e882b7ba-47c6-40f2-89a0-0e08c6b342c5 60197 ms ✔
            └─ Deleting Provision ✔
               └─ DELETE provision and if the service broker is async polling afterwards ✔

Test run finished after 129414 ms
[        11 containers found      ]
[         0 containers skipped    ]
[        11 containers started    ]
[         0 containers aborted    ]
[        11 containers successful ]
[         0 containers failed     ]
[        10 tests found           ]
[         0 tests skipped         ]
[        10 tests started         ]
[         0 tests aborted         ]
[        10 tests successful      ]
[         0 tests failed          ]

```
