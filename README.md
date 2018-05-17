# osb-checker-kotlin

To run a test class specify the it on the command line.
Possibles commands are:

catalog
provision
binding
authentication
contract

example : java -jar osb-checker-kotlin-1.0-SNAPSHOT.jar catalog provision
will run the catalog test and the provision test.

Service Broker related data needs to be entered via YML:
Example

checker:
  url: http://example-dev.cf.dev.eu-de-central.msh.host
  port: 80
  apiVersion: 2.13
  user: admin
  password: cloudfoundry
  authentication: basic
 ...

