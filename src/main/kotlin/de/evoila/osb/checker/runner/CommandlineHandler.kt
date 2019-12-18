package de.evoila.osb.checker.runner

import de.evoila.osb.checker.tests.BindingJUnit5
import de.evoila.osb.checker.tests.CatalogJUnit5
import de.evoila.osb.checker.tests.DataConsistencyJUnit5
import de.evoila.osb.checker.tests.ProvisionJUnit5
import de.evoila.osb.checker.tests.contract.AuthenticationJUnit5
import de.evoila.osb.checker.tests.contract.ContractJUnit5
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.discovery.DiscoverySelectors

class CommandlineHandler {

    private fun setUpOptions(): Options {
        return Options().apply {
            optionObjects.forEach {
                this.addOption(Option.builder(it.opt).longOpt(it.longOpt).desc(it.desc).build())
            }
        }
    }

    private val optionObjects = listOf(
            OptionObject("cat", CATALOG, "Indicate if the Catalog Test should run."),
            OptionObject("prov", PROVISION, "Indicate if the Provision Test should run."),
            OptionObject("bind", BINDING, "Indicate if the Binding Test should run."),
            OptionObject("auth", AUTHENTICATION, "Indicate if the Authentication Test should run."),
            OptionObject("con", CONTRACT, "Indicate if the Contract Test should run."),
            OptionObject("data", DATA, "Indicate if the Data Consistency Test should run."),
            OptionObject("help", HELP, "Display all possible Flags."),
            OptionObject("not", NO_TEST, "Runs the application without doing anything.")
    )

    private val options = setUpOptions()
    private val parser = DefaultParser()

    fun getDiscoverySelectorsOrPrintHelp(args: Array<String>): MutableList<DiscoverySelector> {

        if (args.isEmpty()) {
            return mutableListOf(
                    DiscoverySelectors.selectClass(CatalogJUnit5::class.java),
                    DiscoverySelectors.selectClass(ProvisionJUnit5::class.java),
                    DiscoverySelectors.selectClass(BindingJUnit5::class.java),
                    DiscoverySelectors.selectClass(AuthenticationJUnit5::class.java),
                    DiscoverySelectors.selectClass(ContractJUnit5::class.java),
                    DiscoverySelectors.selectClass(DataConsistencyJUnit5::class.java)
            )
        }

        val commandLine = parser.parse(options, args)
        val selectors = mutableListOf<DiscoverySelector>()

        if (commandLine.hasOption(HELP)) {
            printHelpText()
            return mutableListOf()
        }

        if (commandLine.hasOption(NO_TEST)) {
            println("So? Still satisfied with your service-broker?")
            return mutableListOf()
        }

        if (commandLine.hasOption(CATALOG)) {
            selectors.add(DiscoverySelectors.selectClass(CatalogJUnit5::class.java))
        }

        if (commandLine.hasOption(PROVISION)) {
            selectors.add(DiscoverySelectors.selectClass(ProvisionJUnit5::class.java))
        }

        if (commandLine.hasOption(BINDING)) {
            selectors.add(DiscoverySelectors.selectClass(BindingJUnit5::class.java))
        }

        if (commandLine.hasOption(AUTHENTICATION)) {
            selectors.add(DiscoverySelectors.selectClass(AuthenticationJUnit5::class.java))
        }

        if (commandLine.hasOption(CONTRACT)) {
            selectors.add(DiscoverySelectors.selectClass(ContractJUnit5::class.java))
        }

        if (commandLine.hasOption(DATA)) {
            selectors.add(DiscoverySelectors.selectClass(DataConsistencyJUnit5::class.java))
        }

        return selectors
    }

    private fun printHelpText() {
        println(
                "usage: osb-checker-kotlin\n" +
                        "        -cat,  -catalog           Test that the service broker returns a valid catalog.\n" +
                        "        -prov, -provision         Runs different invalid provision rest calls to check if\n" +
                        "                                  the service-broker acts accordingly.\n" +
                        "        -bind, -binding           Starts with valid provisions and if the service is bindable\n" +
                        "                                  tries to bind on it.\n" +
                        "        -auth, -authentication    Ensures that all endpoints return 401, when trying to access the\n" +
                        "                                  service broker with bad credentials.\n" +
                        "        -con, -contract           Validates that the service broker returns 422 when no api-version is\n" +
                        "                                  provided.\n" +
                        "        -data, -data-consistency  Tests how the service broker behaves when a service instance \n" +
                        "                                  gets deleted, while bindings are present.\n" +
                        "        -not, -no-test            runs nothing. Just for build and compile proposes.\n" +
                        "        -help                     Shows this page.\n \n" +
                        " The service-broker you wish to test needs to be defined within the application.yml file. \n" +
                        " Further documentation can be found here https://github.com/evoila/osb-checker-kotlin."
        )
    }

    data class OptionObject(
            val opt: String,
            val longOpt: String,
            val desc: String
    )

    companion object {
        private const val CATALOG = "catalog"
        private const val PROVISION = "provision"
        private const val BINDING = "binding"
        private const val AUTHENTICATION = "authentication"
        private const val CONTRACT = "contract"
        private const val DATA = "data-consistency"
        private const val NO_TEST = "no-test"
        private const val HELP = "help"
    }
}