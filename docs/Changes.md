
## Changes

### Changes since v1.0:
- Added 2.15 feature testing:
    - Using [maintenance_info](docs/ProvisionTests.md#version-specific-tests) if defined in catalog.
    - Option to set [X-Broker-API-Request-Identity](docs/Usage.md#Configuration) header.
    - When testing polling maximum polling duration is tested.
- Added Option to set [X-Broker-API-Originating-Identity](docs/Usage.md#originating-identity).
- Added checks for osb-error-codes in response bodies.
- Added tests for fetching non existing bindings / provisions.
- Tests for what happens, when trying to create already existing provisions / bindings.
- Various improvements of logging such as using names of services and plans instead of id.
- setting up a custom catalog requires only the id's of the plan and no more additional information. This is gathered from the catalog now.
- Add Optional test for Dashboard URLs.
- Restructuring of binding test:
 - All plans in the catalog are now tested for invalid binding attempts.
 - Too reduce runtime invalid and valid binding tests use the same provision for testing instead.
 
## Hotfix:

#### v1.1.2:
- Auth token now generated in RequestHandler instead of configuration class, to avoid Application Context failure.

#### v1.1.1:
- swap service id and/or plan id, when testing service broker behaviour for conflicting binding.
Test wont be executed when, only one plan is listed in the actual catalog.    (the swap is not influenced by service plans defined in the config)
