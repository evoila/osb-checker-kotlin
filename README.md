# osb-checker-kotlin

Example: `java -jar osb-checker-kotlin-1.0-SNAPSHOT.jar -U https://url.to.the.service.broker -P 443 -u admin -p password -a v2.13 -provision`
will run the the provision test.

Options:
* User: -u/-user
* Password: -p/-password
* URL: -U/-url
* Port: -P/-port
* API: -a/-api


There are five different options to run tests. Possibles commands are:

* catalog: -cat/-catalog
* provision: -prov/-provision
* binding: -bind/-binding
* authentication: -auth/-authentication
* contract: -con/-contract

Catalog
- will call v2/catalog and check if it returns 200.

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
-runs all requests and checks if they fail with 412 if the X-Broker-API-Version header is missing.