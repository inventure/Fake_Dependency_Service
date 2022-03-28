package co.tala.api.fakedependency.constant

import com.fasterxml.jackson.annotation.JsonProperty

enum class VerifyMockContent {

    // verify returns a list of the request payloads in string format
    LIST,

    // verify returns the last request payload in binary format
    LAST,

    // verify returns a list of request payloads in json format
    DETAILED;

    companion object {
        // The query key that defines the mock verification
        const val QUERY_PARAM_KEY = "verifyMockContent"
    }
}
