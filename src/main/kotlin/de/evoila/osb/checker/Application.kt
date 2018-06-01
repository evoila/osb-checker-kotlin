package de.evoila.osb.checker

import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.tests.*
import de.evoila.osb.checker.tests.contract.AuthenticationJUnit5
import de.evoila.osb.checker.tests.contract.ContractJUnit5
import util.ColoredPrintingTestListener
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.SummaryGeneratingListener
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.io.PrintWriter

@SpringBootApplication
class Application

fun main(args: Array<String>) {

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
  }

  val selectors = mutableListOf<DiscoverySelector>()

  if (commandLine.hasOption("catalog")) {
    selectors.add(DiscoverySelectors.selectClass(CatalogJUnit5::class.java))
  }

  if (commandLine.hasOption("provision")) {
    selectors.add(DiscoverySelectors.selectClass(ProvisionJUnit5::class.java))
  }

  if (commandLine.hasOption("binding")) {
    selectors.add(DiscoverySelectors.selectClass(BindingJUnit5::class.java))
  }

  if (commandLine.hasOption("authentication")) {
    selectors.add(DiscoverySelectors.selectClass(AuthenticationJUnit5::class.java))
  }

  if (commandLine.hasOption("contract")) {
    selectors.add(DiscoverySelectors.selectClass(ContractJUnit5::class.java))
  }

  val l = ColoredPrintingTestListener()
  val r = SummaryGeneratingListener()
  val launcher = LauncherFactory.create()

  launcher.registerTestExecutionListeners(l, r)

  val request = LauncherDiscoveryRequestBuilder.request()
      .selectors(
          selectors
      )
      .build()

  launcher.execute(request)
  r.summary.printTo(PrintWriter(System.out))

  System.exit(
      r.summary.failures.size
  )
}