# osb-checker-kotlin

Example: `java -jar osb-checker-kotlin-1.0-SNAPSHOT.jar -U https://url.to.the.service.broker -P 443 -u admin -p password -a v2.13 -provision`
will run the the provision test.

Options:
User: -u/-user
Password: -p/-password
URL: -U/-url
Port: -P/-port
API: -a/-api


There are five different options to run tests. Possibles commands are:

* catalog: -cat/-catalog
* provision: -prov/-provision
* binding: -bind/-binding
* authentication: -auth/-authentication
* contract: -con/-contract


