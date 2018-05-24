package de.evoila.osb.checker

import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.tests.*
import de.evoila.osb.checker.tests.contract.AuthenticationTest
import de.evoila.osb.checker.tests.contract.ContractTest
import de.evoila.osb.checker.util.ColoredPrintingTestListener
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.junit.internal.TextListener
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.LoggingListener
import org.junit.platform.launcher.listeners.SummaryGeneratingListener
import org.junit.runner.JUnitCore
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.io.FileWriter
import java.io.PrintWriter
import java.util.logging.Level

@SpringBootApplication
class Application

fun main(args: Array<String>) {
/*
  val options = Options()
      .apply {
        addOption(
            Option.builder("U")
                .required()
                .longOpt("url")
                .hasArg()
                .desc("the URl of the service broker")
                .build()
        )
        addOption(
            Option.builder("P")
                .required()
                .longOpt("port")
                .hasArg()
                .desc("the port on which the service broker is reachable.")
                .build()
        )
        addOption(
            Option.builder("u")
                .required()
                .longOpt("user")
                .hasArg()
                .desc("the User for the Service Broker.")
                .build()
        )
        addOption(
            Option.builder("p")
                .required()
                .longOpt("password")
                .hasArg()
                .desc("The password tp access the service broker.")
                .build()
        )
        addOption(
            Option.builder("a")
                .required()
                .longOpt("api")
                .hasArg()
                .desc("Thr api version of the service broker")
                .build()
        )
        addOption(Option.builder("I")
            .longOpt("instances")
            .hasArg()
            .desc("The number of Services to test in the Provision Test. Default is 3")
            .build()
        )
        addOption(
            Option.builder("cat")
                .longOpt("catalog")
                .desc("Indicate if the Catalog Test should run.")
                .build()
        )
        addOption(
            Option.builder("prov")
                .longOpt("provision")
                .desc("Indicate if the Provision Test should run.")
                .build()
        )
        addOption(
            Option.builder("bind")
                .longOpt("binding")
                .desc("Indicate if the Binding Test should run.")
                .build()
        )
        addOption(
            Option.builder("auth")
                .longOpt("authentication")
                .desc("Indicate if the Authentication Test should run.")
                .build()
        )
        addOption(Option.builder("con")
            .longOpt("contract")
            .desc("Indicate if the Contract Test should run.")
            .build()
        )
      }

  val parser = DefaultParser()
  val commandLine = parser.parse(options, args)

  Configuration.apply {
    url = commandLine.getOptionValue("url")
    port = commandLine.getOptionValue("port").toInt()
    apiVersion = commandLine.getOptionValue("api")
    user = commandLine.getOptionValue("user")
    password = commandLine.getOptionValue("password")

    if (commandLine.hasOption("I")) {
      maxServices = commandLine.getOptionValue("I").toInt() - 1
    }
  }
*/


  val request = LauncherDiscoveryRequestBuilder.request()
      .selectors(
          DiscoverySelectors.selectPackage("tests")
          , DiscoverySelectors.selectClass(BindingJUnit5::class.java)
      )
      .build()

  val l = ColoredPrintingTestListener()
  val r = SummaryGeneratingListener()
  val launcher = LauncherFactory.create()

  launcher.registerTestExecutionListeners(l, r)

  launcher.execute(request)
  r.summary.printTo(PrintWriter(System.out))


  var failureCount = 0


  //LauncherDiscoveryRequestBuilder


  /*
  if (commandLine.hasOption("catalog")) {
    failureCount += jUnitCore.run(CatalogTests::class.java).failureCount

  }

  if (commandLine.hasOption("provision")) {
    failureCount += jUnitCore.run(ProvisionTests::class.java).failureCount

  }

  if (commandLine.hasOption("binding")) {
    failureCount += jUnitCore.run(BindingTest::class.java).failureCount
  }

  if (commandLine.hasOption("authentication")) {
    failureCount += jUnitCore.run(AuthenticationTest::class.java).failureCount
  }

  if (commandLine.hasOption("contract")) {
    failureCount += jUnitCore.run(ContractTest::class.java).failureCount
  }

  System.exit(failureCount)*/
}