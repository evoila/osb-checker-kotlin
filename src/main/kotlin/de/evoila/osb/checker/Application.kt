package de.evoila.osb.checker

import de.evoila.osb.checker.runner.CommandlineHandler
import de.evoila.osb.checker.util.TreePrintingListener
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.LoggingListener
import org.junit.platform.launcher.listeners.SummaryGeneratingListener
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.io.PrintWriter
import java.util.logging.Level
import kotlin.system.exitProcess

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    val commandlineHandler = CommandlineHandler()
    val selectors = commandlineHandler.getDiscoverySelectorsOrPrintHelp(args)

    if (selectors.isNotEmpty()) {
        val summaryGenerator = SummaryGeneratingListener()
        val launcher = LauncherFactory.create()
        val treeLogger = TreePrintingListener()
        launcher.registerTestExecutionListeners(
                LoggingListener.forJavaUtilLogging(Level.INFO),
                treeLogger,
                summaryGenerator
        )
        val request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectors)
                .build()
        launcher.execute(request)
        summaryGenerator.summary.printTo(PrintWriter(System.out))

        exitProcess(summaryGenerator.summary.failures.size)
    }
}
