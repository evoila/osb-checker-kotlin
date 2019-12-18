# Table of Contents
- [Description](../README.md#description)
- [Getting Started](../README.md#getting-started)
    - [Build Application](../README.md#build-application)
    - [Basic Run Configuration](../README.md#basic-run-configuration)
- [Changes](../README.md#changes)
- [Usage](Usage.md)
    - [Declaring Test Runs](Usage.md#declaring-test-runs)
    - [Configuration](Usage.md#configuration)
    - [Parameters](Usage.md#parameters)
    - [Originating Identity](Usage.md#originating-identity)
    - [Declaring Services](Usage.md#declaring-services)
- Test Classes
    - [Catalog](CatalogTest.md)
       - [Example Output](CatalogTest.md#example-output)
    - [Provision](ProvisionTests.md#provision-tests)
        - [Test Procedure](ProvisionTests.md#test-procedure)
        - [Version specific Tests](ProvisionTests.md#version-specific-tests)
        - [Example Output](ProvisionTests.md#example-output)
    - [Binding](#binding-tests)
        - [Test Procedure](BindingTests.md#test-procedure)
        - [Version specific Tests](BindingTests.md#version-specific-tests)
        - [Example Output](BindingTests.md#example-output)
    - [Authentication](docs/AuthenticationTests.md)   
    - [Contract](docs/ContractTest.md)
    - [Data Consistency Check](#data-consistency-check)
        - [Test Procedure](#test-procedure)
        - [Example Output](#example-output)
- [Contribution](docs/Contribution.md)
   
# Data Consistency Check

The Data Consistency Check tests if creating instances and bindings with the same instanceId or bindingId,
works if the service instance got deleted. The goal is to ensure that all binding on service doesn't remain in the database of the service broker.
This is done for all plans in the catalog. bindings and provisions are created based upon the
api-version and configuration like in [Binding Tests](BindingTests.md).


## Test Procedure

- Valid Provisioning and Bindings
    - Create valid provisioning.
        - If the Service broker creates service instances asynchronously, the checker will start polling and verify the responses are according
         to [spec](https://github.com/openservicebrokerapi/servicebroker/blob/v2.15/spec.md#polling-last-operation-for-service-instances) and finish successfully.
        - When configured to do so, the checker verifies if the dashboard URL works.
        - Test what happens when attempting to create a service instance with the same instance id and same parameters and with different parameters.
     Read [here](https://github.com/openservicebrokerapi/servicebroker/blob/v2.15/spec.md#polling-last-operation-for-service-instances) about the expected behaviour.
    - If the Service is bindable. Look [here](https://github.com/openservicebrokerapi/servicebroker/blob/v2.15/spec.md#binding) on how it should behave.
        - Runs a valid binding on the service.
    - Delete the provision
    - Repeats the previous steps with the same instance and binding Id again.
    
## ExampleOutput

```
╷
└─ JUnit Jupiter ✔
   └─ Database Consistency Check ✔
      └─ Ensure a clean deletion of instances with bindings. ✔
         └─ Test if reusing instance and binding ids is possible, if service instance with binding gets deleted ✔
            ├─ Creating first provision, bind on it and delete the service instance afterwards. ✔
            │  ├─ Creating Service Instance. ✔
            │  │  ├─ Running valid PUT provision with instanceId 36374785-a9a9-4e47-a9c6-36d5679eee3f for service 'base-sql-service-dev-managed' and plan 's' ✔
            │  │  ├─ Running valid PUT provision with same attributes again. Expecting Status 200. ✔
            │  │  ├─ Running valid PUT provision with different space_guid. Expecting Status 409. ✔
            │  │  ├─ Running valid PUT provision with different organization_guid. Expecting Status 409. ✔
            │  │  └─ Running valid PUT provision with different plan and service Id. Expecting Status 409. ✔
            │  ├─ Binding on bindable service instance. ✔
            │  │  ├─ Running valid PUT binding with bindingId 3d5dd356-78d8-4a9c-abd8-344acb0b9f90 ✔
            │  │  ├─ Running PUT binding with same attribute again. Expecting StatusCode 200. ✔
            │  │  └─ Running PUT binding with different attribute again. Expecting StatusCode 409. ✔
            │  └─ Deleting provision ✔
            │     ├─ DELETE provision and if the service broker is async polling afterwards ✔
            │     └─ Running valid DELETE provision with same parameters again. Expecting Status 410. ✘ 1 expectation failed.
            │              Expected status code one of {<410>} but was <500>.
            └─ Attempting to provision, bind and delete the service instance again. ✔
               ├─ Creating Service Instance. ✔
               │  ├─ Running valid PUT provision with instanceId 36374785-a9a9-4e47-a9c6-36d5679eee3f for service 'base-sql-service-dev-managed' and plan 's' ✔
               │  ├─ Running valid PUT provision with same attributes again. Expecting Status 200. ✔
               │  └─ Running valid PUT provision with different attributes again. Expecting Status 409. ✔
               ├─ Binding on bindable service instance. ✔
               │  ├─ Running valid PUT binding with bindingId 3d5dd356-78d8-4a9c-abd8-344acb0b9f90 ✔
               │  ├─ Running PUT binding with same attribute again. Expecting StatusCode 200. ✔
               │  └─ Running PUT binding with different attribute again. Expecting StatusCode 409. ✔
               └─ Deleting provision ✔
                  ├─ DELETE provision and if the service broker is async polling afterwards ✔
                  └─ Running valid DELETE provision with same parameters again. Expecting Status 410. ✔

Test run finished after 9323 ms
[        12 containers found      ]
[         0 containers skipped    ]
[        12 containers started    ]
[         0 containers aborted    ]
[        12 containers successful ]
[         0 containers failed     ]
[        16 tests found           ]
[         0 tests skipped         ]
[        16 tests started         ]
[         0 tests aborted         ]
[        16 tests successful      ]
[         0 tests failed          ]
```