# osb-checker-kotlin

# Table of Contents
- [Description](#Description)
- [Usage](#Usage)
    - [Configuration](#Configuration)
    - [Parameters](#Parameters)
    - [Declaring Services](#Declaring Services)
    - [Declaring Test Runs](#Declaring Test Runs)
- [Test](#Test)
    - [Catalog](#Catalog)
    - [Provision](#Provision)
    - [Binding](#Binding)
    - [Authentication](#Authentication)
    - [Contract](#Contract)
    - [Example output](#Example output)


###Description

This application is a generalized test program for service brokers. It runs rest calls against the defined service broker and checks if it
behaves as expected to the [service broker API specification](link=https://github.com/openservicebrokerapi/servicebroker)  
Tests are created dynamically based upon the service broker catalog or custom input by the operator.

###Usage

####Configuration

To run the application put a file with the name .yml file into the same location as the osb-checker-kotlin-1.0.jar file with the following schema

```yaml

##Define the service broker connection here
config:
  url: http://localhost
  port : 80
  apiVersion: 2.13
  user: user
  password: password
##Optional
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

####Parameters

To set parameters for the provision, define them in parameters (Default is null).
specify the plan id as key for the parameters
example: a configuration with ...

```yaml
parameters:
    plan-id-here:
      DB-name: "db-name
      parameter1 : 1
      parameter2 : foo
      key : value:
      schemaName: a_name
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
    "DB-name": "db-name",
    "parameter1" : 1,
    "parameter2" : "foo",
    "key" : "value",
    "schemaName": "a_name"
    
    }
}
```
` -X PUT -H "X-Broker-API-Version: api-version-here" -H "Content-Type: application/json"`

####Declaring Services

To define a specific set of services and plans for testing define them under services like this:

```yaml
     services:
       - id: service-id-here
         plans:
           - id: plan-id-here
           - id: plan-id2-here
             bindable: false
         bindable: true     
       - id: service-id2-here
         plans:
          - id: plan-id3-here
         bindable: false   
```

this config would run the following provisions and bindings when running the binding test:

- `curl http://username:password@broker-url/v2/service_instances/:instance_id?accepts_incomplete=true -d `
```json
{
  "service_id": "service-id-here",
  "plan_id": "plan-id-here",
  "organization_guid": "org-guid-here",
  "space_guid": "space-guid-here"
}
```
` -X PUT -H "X-Broker-API-Version: api-version-here" -H "Content-Type: application/json"`

- `curl http://username:password@broker-url/v2/service_instances/:instance_id/service_bindings/:binding_id?accepts_incomplete=true -d `
```json
{
  "service_id": "service-id-here",
  "plan_id": "plan-id-here",
  "organization_guid": "org-guid-here",
  "space_guid": "space-guid-here"
}
```
` -X PUT -H "X-Broker-API-Version: api-version-here" -H "Content-Type: application/json"`

- `curl http://username:password@broker-url/v2/service_instances/:instance_id?accepts_incomplete=true -d `
```json
{
  "service_id": "service-id2-here",
  "plan_id": "plan-id-here",
  "organization_guid": "org-guid-here",
  "space_guid": "space-guid-here"
}
```
` -X PUT -H "X-Broker-API-Version: api-version-here" -H "Content-Type: application/json"`


- `curl http://username:password@broker-url/v2/service_instances/:instance_id?accepts_incomplete=true -d `
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

####Declaring Test Runs

There are five different options to run tests. Possibles commands are:

* catalog: -cat/-catalog
* provision: -prov/-provision
* binding: -bind/-binding
* authentication: -auth/-authentication
* contract: -con/-contract

In case you want to run all tests call for example `java -jar osb-checker-kotlin-1.0.jar -cat -provision -bind -auth -con`

###Test

All Tests make a series of rest calls on the defined service and validate their behaviour afterwards.
Some Tests need certain endpoints to work as defined in the osb specification.
For example: The binding test can only run if the provisioning and catalog endpoint work.
It is recommended to use the checker accordingly the ease up debugging of the service broker.

####Catalog

when starting the application with the parameter -cat/-catalog, it will:
- call `curl http://username:password@broker-url/v2/catalog -X PUT -H "X-Broker-API-Version: api-version-here" -H "Content-Type: application/json"`
- check if the service broker returns 200 and validate if the catalog from the response follows schema.

Look [here](https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#catalog-management), for more information about service broker catalogs.

####Provision

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

####Binding

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

Look [here](https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#binding) for more information about binding and unbinding.


####Authentication

- runs a all requests without a valid password and checks if the fails with HttpStatus 401 unauthorized.

####Contract

- runs all standard requests and checks if they fail with 412 Precondition Failed, if the X-Broker-API-Version header is missing or does not match the given one.

####Example output
A Binding Test output with one deployed service.
```
╷
└─ JUnit Jupiter ✔
   └─ BindingJUnit5 ✔
      ├─ runInvalidBindingAttempts() ✔
      │  ├─ Provision and in case of a Async SB polling, for later binding ✔
      │  │  └─ Running Valid Provision with InstanceId d78baaef-d602-4222-b9f5-c0dd74198560 ✔
      │  ├─ PUT should reject if missing service_id ✔
      │  ├─ DELETE should reject if missing service_id ✔
      │  ├─ PUT should reject if missing plan_id ✔
      │  ├─ DELETE should reject if missing plan_id ✔
      │  └─ Deleting Provision and Polling afterwards ✔
      │     └─ deleting Provision ✔
      └─ runValidBindings() ✔
         └─ Running a valid provision with instanceId 6430daf0-5231-49e6-a9d9-58f6625d3813 and if it is bindable a valid binding with bindingId 77fc6c32-b23a-4577-b24e-8f3347eb21c7 ✔
            ├─ Provision and in case of a Async SB polling, for later binding ✔
            │  └─ Running Valid Provision with InstanceId 6430daf0-5231-49e6-a9d9-58f6625d3813 ✔
            ├─ Running PUT Binding and DELETE Binding afterwards ✔
            │  ├─ PUT Binding ✔
            │  └─ DELETE Binding ✔
            └─ Deleting Provision and Polling afterwards ✔
               └─ deleting Provision ✔

Test run finished after 12060 ms
[        10 containers found      ]
[         0 containers skipped    ]
[        10 containers started    ]
[         0 containers aborted    ]
[        10 containers successful ]
[         0 containers failed     ]
[        10 tests found           ]
[         0 tests skipped         ]
[        10 tests started         ]
[         0 tests aborted         ]
[        10 tests successful      ]
[         0 tests failed          ]
```