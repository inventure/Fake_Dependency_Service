package co.tala.api.fakedependency.business.parser

import co.tala.api.fakedependency.configuration.jackson.JacksonConfiguration
import co.tala.api.fakedependency.exception.PayloadTypeNotSupportedException
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class PayloadParserSpec extends Specification {
    private class InvalidModel {}
    private static final NESTED_XML = """<?xml version="1.0" encoding="UTF-8"?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.reporteCC.cc.com/"><soapenv:Header /><soapenv:Body><ws:someWs><arg0><![CDATA[<?xml version="1.0" encoding="ISO-8859-1"?><InnerXml><Deeper>foo</Deeper></InnerXml>]]></arg0></ws:someWs></soapenv:Body></soapenv:Envelope>"""
    private static final SIMPLE_XML = """<?xml version="1.0" encoding="UTF-8"?><Root><Simple><Path>bar</Path></Simple></Root>"""
    private static final MAP = ["root": ["child1": 5, "child2": "15", "childWithChildren": ["animal": "dog", "number": 10]], "leaf": 100]
    private JacksonConfiguration jacksonConfiguration
    private IPayloadParser sut

    def setup() {
        jacksonConfiguration = new JacksonConfiguration()
        sut = new PayloadParser(jacksonConfiguration.mapper(), jacksonConfiguration.xmlMapper())
    }

    def "parse should get a value from SOAP XML with key #key"() {
        when: "the xml is parsed with key #key"
            String result = sut.parse(xml, key)

        then: "the result should be #expected"
            result == expected

        where:
            xml        | key                       | expected
            NESTED_XML | "Body.someWs.arg0.Deeper" | "foo"
            SIMPLE_XML | "Simple.Path"             | "bar"
    }

    def "parse should get a value from a Map"() {
        when: "the map is parsed with key #key"
            String result = sut.parse(MAP, key)

        then: "the result should be #expected"
            result == expected

        where:
            key                             | expected
            "root.child1"                   | "5"
            "root.child2"                   | "15"
            "root.childWithChildren.animal" | "dog"
            "root.childWithChildren.number" | "10"
            "leaf"                          | "100"
            "no.such.leaf"                   | null
    }

    def "parse should throw exception if the type passed is not an XML String nor Map"() {
        when: "a payload of #payload is passed"
            sut.parse(payload, "does.not.matter")

        then: "a PayloadTypeNotSupportedException should be thrown"
            thrown PayloadTypeNotSupportedException

        where:
            payload << [5, 0.4, 10L, 0.9f, new InvalidModel()]
    }

}
