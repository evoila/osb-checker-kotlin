package de.evoila.osb.checker.tests

import org.junit.jupiter.api.Test


class CatalogTests : TestBase() {

  @Test
  fun validateCatalog() {
    wire()
    catalogRequestRunner.correctRequestAndValidateResponse()
  }
}