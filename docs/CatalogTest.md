## Catalog Test

The Catalog Tests verifies that catalog endpoint returns status-code 200 OK and a valid service broker catalog according to spec.

when starting the application with the parameter -cat/-catalog, it will:
- call `curl http://username:password@broker-url/v2/catalog -X GET -H "X-Broker-API-Version: api-version-here" -H "Content-Type: application/json"`
- check if the service broker returns 200 and validate if the catalog from the response follows schema.

Look [here](https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#catalog-management), for more information about service broker catalogs.
A valid catalog is crucial for all following tests, since the checker uses it's content to figure out what tests should run. It's highly recommended to make sure
the catalog is implemented correctly before continuing implementing the other endpoints.

### Example Output

```
╷
└─ JUnit Jupiter ✔
   └─ CatalogJUnit5 ✔
      └─ validateCatalog() ✔

```