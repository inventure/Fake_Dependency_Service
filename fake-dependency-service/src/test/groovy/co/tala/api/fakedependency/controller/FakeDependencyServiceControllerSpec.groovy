package co.tala.api.fakedependency.controller

import co.tala.api.fakedependency.business.provider.IFakeDependencyProvider
import co.tala.api.fakedependency.model.MockData
import co.tala.api.fakedependency.testutil.MockDataFactory
import org.springframework.http.ResponseEntity
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

class FakeDependencyServiceControllerSpec extends Specification {
    private MockData mockData
    private IFakeDependencyProvider providerMock
    private HttpServletRequest requestMock
    private FakeDependencyServiceController sut

    def setup() {
        mockData = MockDataFactory.buildDefaultMockData()
        providerMock = Mock()
        requestMock = Mock()
        sut = new FakeDependencyServiceController(providerMock)
    }

    def "setUp should invoke provider.setUp"() {
        given:
            ResponseEntity<MockData> mockResponse = ResponseEntity.ok(mockData)
            1 * providerMock.setup(mockData, requestMock) >> mockResponse

        when:
            def result = sut.setUp(mockData, requestMock)

        then:
            result == mockResponse
    }

    def "patchSetup should invoke provider.patchSetup"() {
        given:
            ResponseEntity<Object> mockResponse = ResponseEntity.noContent().build()
            1 * providerMock.patchSetup(requestMock) >> mockResponse

        when:
            def result = sut.patchSetup(requestMock)

        then:
            result == mockResponse
    }

    def "post should invoke provider.execute"() {
        given:
            ResponseEntity<Object> mockResponse = ResponseEntity.ok(mockData.responseBody)
            1 * providerMock.execute(requestMock) >> mockResponse

        when:
            def result = sut.post(requestMock)

        then:
            result == mockResponse
    }

    def "put should invoke provider.execute"() {
        given:
            ResponseEntity<Object> mockResponse = ResponseEntity.ok(mockData.responseBody)
            1 * providerMock.execute(requestMock) >> mockResponse

        when:
            def result = sut.put(requestMock)

        then:
            result == mockResponse
    }

    def "patch should invoke provider.execute"() {
        given:
            ResponseEntity<Object> mockResponse = ResponseEntity.ok(mockData.responseBody)
            1 * providerMock.execute(requestMock) >> mockResponse

        when:
            def result = sut.patch(requestMock)

        then:
            result == mockResponse
    }

    def "get should invoke provider.execute"() {
        given:
            ResponseEntity<Object> mockResponse = ResponseEntity.ok(mockData.responseBody)
            1 * providerMock.execute(requestMock) >> mockResponse

        when:
            def result = sut.get(requestMock)

        then:
            result == mockResponse
    }

    def "delete should invoke provider.execute"() {
        given:
            ResponseEntity<Object> mockResponse = ResponseEntity.ok(mockData.responseBody)
            1 * providerMock.execute(requestMock) >> mockResponse

        when:
            def result = sut.delete(requestMock)

        then:
            result == mockResponse
    }

    def "verify should invoke provider.verify"() {
        given:
            def response = [["key": "value"]]
            ResponseEntity<Object> mockResponse = ResponseEntity.ok(response)
            1 * providerMock.verify(requestMock) >> mockResponse

        when:
            def result = sut.verify(requestMock)

        then:
            result == mockResponse
    }
}
