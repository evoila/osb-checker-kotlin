package de.evoila.osb.checker.tests

import org.junit.jupiter.api.Test


class CatalogJUnit5 : TestBase() {

  @Test
  fun validateCatalog() {
    wire()
    catalogRequestRunner.correctRequestAndValidateResponse()
  }
}