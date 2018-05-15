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
  token: Basic YWRtaW46Y2xvdWRmb3VuZHJ5
  authentication: basic
 ...

As rest assure didn't support basic auth properly the authentication token needs to be entered maunually in the yml file

