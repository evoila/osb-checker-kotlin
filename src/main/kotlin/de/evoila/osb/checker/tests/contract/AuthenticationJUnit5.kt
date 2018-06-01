package de.evoila.osb.checker.tests.contract

import de.evoila.osb.checker.request.BindingRequestRunner
import de.evoila.osb.checker.tests.TestBase
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired


class AuthenticationJUnit5 : TestBase() {
  @Autowired
  lateinit var bindingRequestRunner: BindingRequestRunner

  @TestFactory
  fun testAuthentication(): List<DynamicNode> {
    wire()

    return listOf(
        DynamicContainer.dynamicContainer("Requests been rejected without authentication:",
            listOf(
                DynamicTest.dynamicTest("GET - v2/catalog should reject with 412")
                { catalogRequestRunner.noAuth() },
                DynamicTest.dynamicTest("PUT - v2/service_instance/instance_id should reject with 412")
                { provisionRequestRunner.putNoAuth() },
                DynamicTest.dynamicTest("GET - v2/service_instance/instance_id/last_operation should reject with 412")
                { provisionRequestRunner.lastOpNoAuth() },
                DynamicTest.dynamicTest("PUT - v2/service_instance/instance_id/service_binding/binding_id  should reject with 412)")
                { provisionRequestRunner.putNoAuth() },
                DynamicTest.dynamicTest("DELETE - v2/service_instance/instance_id/service_binding/binding_id  should reject with 412)")
                { provisionRequestRunner.deleteNoAuth() },
                DynamicTest.dynamicTest("PUT - v2/service_instance/instance_id/service_binding/binding_id  should reject with 412")
                { bindingRequestRunner.putNoAuth() },
                DynamicTest.dynamicTest("DELETE - v2/service_instance/instance_id/service_binding/binding_id  should reject with 412")
                { bindingRequestRunner.deleteNoAuth() }
            )
        )
    )
  }
}