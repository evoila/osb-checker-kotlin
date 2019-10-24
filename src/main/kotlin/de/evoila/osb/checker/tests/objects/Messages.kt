package de.evoila.osb.checker.tests.objects

object Messages {
    const val TEST_DASHBOARD_DISPLAY_NAME = ", test dashboard URL"
    const val VALID_PROVISION_DISPLAY_NAME = "Creating Service Instance"
    const val DELETE_PROVISION_MESSAGE = "DELETE provision and if the service broker is async polling afterwards"
    const val VALID_FETCH_PROVISION = ", and try to fetch it"
    const val VALID_BINDING_DISPLAY_NAME = "Running valid PUT binding with bindingId "
    const val VALID_BINDING_MESSAGE = "Running PUT binding and DELETE binding afterwards"
    const val DELETE_RESULT_MESSAGE = "Delete has to result in 410"
    const val EXPECTED_FINAL_POLLING_STATE = "Expected the final polling state to be \"succeeded\" but was "

    const val SKIPPING_BINDING_WITH_DIFFERENT_ATTRIBUTES = "%SKIPPED%Skipping PUT Binding with different attributes, because catalog does not contain more than one plan."
}