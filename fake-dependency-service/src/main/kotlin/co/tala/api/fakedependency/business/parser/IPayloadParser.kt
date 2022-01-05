package co.tala.api.fakedependency.business.parser

interface IPayloadParser {
    /**
     * Traverses a payload, given a '.' delimited string as the key path to parse the value
     * The supported payloads are maps and XML String
     */
    fun parse(payload: Any, key: String): String
}
