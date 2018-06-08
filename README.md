# osb-checker-kotlin

This application is a general tests for service brokers. It runs rest calls against the defined service broker and checks if it
behaves as expected to the service broker API specification: [link](https://github.com/openservicebrokerapi/servicebroker)

To run the application put a file with the name application.yml into the same folder as the jar file with the following schema

```yaml
config:
  url: http://localhost
  port : 80
  apiVersion: 2.13
  user: admin
  password: cloudfoundry
  usingAppGuid: true

  parameters:
     plan-id-1-here:
        parameter1 : 1
        parameter2 : foo
      plan-id-2-here:
        parameter1 : 2
        parameter2 : bar
        
     services:
       -
         id: service-id-here
         plans:
           -
             id: plan-id-here
   
         bindable: true     
```

url, port, apiVersion, user and password are mandatory and MUST be set.
usingAppGuid, parameters and services are optional.

usingAppGuid sets the osb-checker to set a appGuid during provisioning. If no value it set it falls back to default true.

##Parameters

To set parameters for the provision, define them in parameters (Default is null).

specify the plan id as key for the parameters

example: a configuration with 

```yaml
parameters:
    plan-id-here:
        DB-Name: db-name
```

would run a provisions like this:

  `curl http://username:password@broker-url/v2/service_instances/:instance_id?accepts_incomplete=true -d `
```json
{
  "service_id": "service-id-here",
  "plan_id": "plan-id-here",
  "organization_guid": "org-guid-here",
  "space_guid": "space-guid-here",
  "parameters": {
    "DB-name": "db-name"
    }
}
```
` -X PUT -H "X-Broker-API-Version: api-version-here" -H "Content-Type: application/json"`

##Declaring Services

To define a specific set of services and plans for testing define them under services like this:

```yaml
     services:
       -
         id: service-id-here
         plans:
           -
             id: plan-id-here
           -
             id: plan-id2-here
             bindable: false
         bindable: true     
       -
        id: service-id2-here
        plans:
          -
            id: plan-id3-here
        bindable: false   
```

this config would run the following PUT calls when running the binding test:

`curl http://username:password@broker-url/v2/service_instances/:instance_id?accepts_incomplete=true -d `
```json
{
  "service_id": "service-id-here",
  "plan_id": "plan-id-here",
  "organization_guid": "org-guid-here",
  "space_guid": "space-guid-here"
}
```
` -X PUT -H "X-Broker-API-Version: api-version-here" -H "Content-Type: application/json"`

`curl http://username:password@broker-url/v2/service_instances/:instance_id/service_bindings/:binding_id?accepts_incomplete=true -d `
```json
{
  "service_id": "service-id-here",
  "plan_id": "plan-id-here",
  "organization_guid": "org-guid-here",
  "space_guid": "space-guid-here"
}
```
` -X PUT -H "X-Broker-API-Version: api-version-here" -H "Content-Type: application/json"`

`curl http://username:password@broker-url/v2/service_instances/:instance_id?accepts_incomplete=true -d `
```json
{
  "service_id": "service-id2-here",
  "plan_id": "plan-id-here",
  "organization_guid": "org-guid-here",
  "space_guid": "space-guid-here"
}
```
` -X PUT -H "X-Broker-API-Version: api-version-here" -H "Content-Type: application/json"`


`curl http://username:password@broker-url/v2/service_instances/:instance_id?accepts_incomplete=true -d `
```json
{
  "service_id": "service-id2-here",
  "plan_id": "plan-id3-here",
  "organization_guid": "org-guid-here",
  "space_guid": "space-guid-here"
}
```
` -X PUT -H "X-Broker-API-Version: api-version-here" -H "Content-Type: application/json"`

If no catalog is set the checker will use the catalog the service broker provides by itself.

##Declaring Test Runs

Example: `java -jar osb-checker-kotlin-1.0.jar -provision`
will run the the provision test.


There are five different options to run tests. Possibles commands are:

* catalog: -cat/-catalog
* provision: -prov/-provision
* binding: -bind/-binding
* authentication: -auth/-authentication
* contract: -con/-contract

Catalog
- will call v2/catalog and check if it returns 200 and validate if the catalog follows schema.

Provision
- Provision will run all v2/instance/instanceId...
- calls the catalog to set up a valid provision request
  validates the brokers behaviour when running sync and that it returns 400 when making a invalid request.
 
Binding
- runs a full scenario of  creating calling the catalog and creates a instance for each plan_id and performs a bind on it.
- afterwards it deletes the binding and the provision
- validates that the broker returns 400 when making invalid binding requests

authentication
-runs a all requests without a valid password and checks if the fails with HttpStatus unauthorized.

contract: 
-runs all requests and checks if they fail with 412 if the X-Broker-API-Version header is missing or does not match the given one.

##Example output

```sh
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