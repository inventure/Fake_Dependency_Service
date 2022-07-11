package co.tala.api.fakedependency.business.parser

interface IPayloadParser {
    /**
     * Traverses a payload, given a '.' delimited string as the key path to parse the value
     * The supported payloads are JSON and XML String
     *
     * NOTE: It is recommended to mock with a unique id from the URI and or request header id
     * and not the payload because parsing a payload is expensive, which impacts on performance.
     * Parsing the payload should be used as a last resort for mock id uniqueness.
     */
    fun parse(payload: Any, key: String): String?
}
