package de.evoila.osb.checker

import de.evoila.osb.checker.config.Configuration
import de.evoila.osb.checker.tests.BindingTest
import de.evoila.osb.checker.tests.CatalogTests
import de.evoila.osb.checker.tests.ProvisionTests
import de.evoila.osb.checker.tests.contract.AuthenticationTest
import de.evoila.osb.checker.tests.contract.ContractTest
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.junit.internal.TextListener
import org.junit.runner.JUnitCore
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.test.context.junit4.SpringRunner


@SpringBootApplication
class Application

fun main(args: Array<String>) {

  val options = Options()
      .apply {
        addOption(
            Option.builder("url")
                .required()
                .longOpt("URL")
                .hasArg()
                .desc("the URl of the service broker")
                .build()
        )
        addOption(
            Option.builder("port")
                .required()
                .longOpt("Port")
                .hasArg()
                .desc("the port on which the service broker is reachable.")
                .build()
        )
        addOption(
            Option.builder("user")
                .required()
                .longOpt("User")
                .hasArg()
                .desc("the User for the Service Broker.")
                .build()
        )
        addOption(
            Option.builder("pw")
                .required()
                .longOpt("Password")
                .hasArg()
                .desc("The password tp access the service broker.")
                .build()
        )
        addOption(
            Option.builder("api")
                .required()
                .longOpt("Api-Version")
                .hasArg()
                .desc("Thr api version of the service broker")
                .build()
        )
        addOption(
            Option.builder("cat")
                .longOpt("Catalog")
                .desc("Indicate if the Catalog Test should run.")
                .build()
        )
        addOption(
            Option.builder("pro")
                .longOpt("Provision")
                .desc("Indicate if the Provision Test should run.")
                .build()
        )
        addOption(
            Option.builder("bin")
                .longOpt("Binding")
                .desc("Indicate if the Binding Test should run.")
                .build()
        )
        addOption(
            Option.builder("auth")
                .longOpt("Authentication")
                .desc("Indicate if the Authentication Test should run.")
                .build()
        )
        addOption(Option.builder("con")
            .longOpt("Contract")
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
    password = commandLine.getOptionValue("pw")
  }

  val jUnitCore = JUnitCore()
  jUnitCore.addListener(TextListener(System.out))

  if (commandLine.hasOption("cat")) {
    jUnitCore.run(CatalogTests::class.java)

  }

  if (commandLine.hasOption("pro")) {
    jUnitCore.run(ProvisionTests::class.java)

  }

  if (commandLine.hasOption("bin")) {
    jUnitCore.run(BindingTest::class.java)

  }

  if (commandLine.hasOption("auth")) {
    jUnitCore.run(AuthenticationTest::class.java)

  }

  if (commandLine.hasOption("con")) {
    jUnitCore.run(ContractTest::class.java)
  }
  System.exit(0)
}