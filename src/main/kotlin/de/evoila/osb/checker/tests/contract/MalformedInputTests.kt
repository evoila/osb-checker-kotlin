package de.evoila.osb.checker.tests.contract

import de.evoila.osb.checker.tests.TestBase
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.slf4j.LoggerFactory
import java.text.CharacterIterator
import kotlin.test.assertTrue

@DisplayName("Checking service-broker behaviour when using strings outside of common/best practice.")
class MalformedInputTests : TestBase() {

    @Test
    @DisplayName("Testing behaviour when values contain characters outside of \"Unreserved Characters\" are used.")
    fun testCatalogEndpointWithMalformedHeaderValues() {
        if (configuration.apiVersion >= 2.15) {
            val value = createStrangeId()
            val response = catalogRequestRunner.withSpecificHeaderAndNoAssertion("X-Broker-API-Request-Identity", value)
            if (response.statusCode() == 200) {
                log.warn("The catalog endpoint accepts X-Broker-API-Request-Identity with values like $value. It may be useful to block such headers.")
            }
        }
    }

    private fun createStrangeId(): String {
        var strangeId = ""
        (0..35).map { uniCodeSymbols.random() }.forEach { strangeId += it }
        return strangeId
    }

    companion object {
        private val genDelims = arrayOf(":", "/", "?", "#", "[", "]", "@")
        private val subDelims = arrayOf("*", "+", ",", ";", "=")
        private val uniCodeSymbols = 123.toChar()..65535.toChar()
        private val log = LoggerFactory.getLogger(MalformedInputTests::class.java)
    }
}