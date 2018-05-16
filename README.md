# osb-checker-kotlin

To run a test class specify the it on the command line.


Arguments are:

required

-url / -URL :  
-port / -Port 
-user / -User
-pw / -Password : 
-api / -Api-Version 

optional:

Add these to run the corresponding test

-cat, -Catalog 
-pro, -Provision
-bin,  -Binding
-auth,  -Authentication
-con, -Contract


Example Command;
java -jar osb-checker-kotlin-1.0-SNAPSHOT.jar -url http://example-dev.cf.dev.eu-de-central.msh.host -port 80 -user admin -pw cloudfoundry -api 2.13  -cat -pro

will run a Catalog and ProvisionTest on the http://example-dev.cf.dev.eu-de-central.msh.host Service Broker.

