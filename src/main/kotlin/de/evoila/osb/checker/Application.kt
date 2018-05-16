package de.evoila.osb.checker

import de.evoila.osb.checker.tests.BindingTest
import de.evoila.osb.checker.tests.CatalogTests
import de.evoila.osb.checker.tests.ProvisionTests
import de.evoila.osb.checker.tests.contract.AuthenticationTest
import de.evoila.osb.checker.tests.contract.ContractTest
import org.junit.runner.Description
import org.junit.runner.JUnitCore
import org.junit.runner.Result
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener
import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication
class Application

fun main(args: Array<String>) {

  val jUnitCore = JUnitCore()

  if ("catalog" in args) {
    val result = jUnitCore.run(CatalogTests::class.java)


    result.failures.forEach {
      print(it.message)
    }

  }

  if ("provision" in args) {
    val result = jUnitCore.run(ProvisionTests::class.java)


    result.failures.forEach {
      println(it.message)
    }
  }

  if ("binding" in args) {
    val result = jUnitCore.run(BindingTest::class.java)

    result.failures.forEach {
      println(it.message)
    }
  }

  if ("authentication" in args) {
    val result = jUnitCore.run(AuthenticationTest::class.java)

    result.failures.forEach {
      println(it.message)
    }
  }

  if ("contract" in args) {
    val result = jUnitCore.run(ContractTest::class.java)

    result.failures.forEach {
      println(it.message)
    }
  }

  System.exit(0)
}
