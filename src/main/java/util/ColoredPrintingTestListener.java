package util;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import static util.ColoredPrintingTestListener.Color.*;

public class ColoredPrintingTestListener implements TestExecutionListener {


    public ColoredPrintingTestListener() {
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        System.out.printf("Test execution started. Number of static tests: %d%n",
                testPlan.countTestIdentifiers(TestIdentifier::isTest));
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        System.out.println("Test execution finished.");
    }

    @Override
    public void dynamicTestRegistered(TestIdentifier testIdentifier) {
        printlnTestDescriptor(BLUE, "Test registered:", testIdentifier);
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        printlnTestDescriptor(YELLOW, "Skipped:", testIdentifier);
        printlnMessage(YELLOW, "Reason", reason);
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        printlnTestDescriptor(NONE, "Started:", testIdentifier);
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        Color color = determineColor(testExecutionResult.getStatus());
        printlnTestDescriptor(color, "Finished:", testIdentifier);
        testExecutionResult.getThrowable().ifPresent(t -> printlnException(color, t));
    }

    @Override
    public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
        printlnTestDescriptor(PURPLE, "Reported:", testIdentifier);
        StringBuilder stringBuilder = new StringBuilder();
        // entry.appendDescription(stringBuilder, "");
        printlnMessage(PURPLE, "Reported values", stringBuilder.toString());
    }

    private Color determineColor(TestExecutionResult.Status status) {
        switch (status) {
            case SUCCESSFUL:
                return GREEN;
            case ABORTED:
                return YELLOW;
            case FAILED:
                return RED;
            default:
                return NONE;
        }
    }

    private void printlnTestDescriptor(Color color, String message, TestIdentifier testIdentifier) {
        println(color, "%-10s   %s [%s]", message, testIdentifier.getDisplayName(), testIdentifier.getUniqueId());
    }

    private void printlnException(Color color, Throwable throwable) {
        printlnMessage(color, "Exception", throwable.getLocalizedMessage());
    }

    private void printlnMessage(Color color, String message, String detail) {
        println(color, "             => " + message + ": %s", detail);
    }

    private void println(Color color, String format, Object... args) {
        println(color, String.format(format, args));
    }

    private void println(Color color, String message) {

        // Use string concatenation to avoid ANSI disruption on console
        System.out.println(color + message + NONE);
    }

    enum Color {
        NONE(0),
        RED(31),
        GREEN(32),
        YELLOW(33),
        BLUE(34),
        PURPLE(35);

        private final int ansiCode;

        Color(int ansiCode) {
            this.ansiCode = ansiCode;
        }

        @Override
        public String toString() {
            return "\u001B[" + this.ansiCode + "m";
        }
    }
}