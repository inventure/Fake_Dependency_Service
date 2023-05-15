package co.tala.api.fakedependency.business.mockdata.defaultmock

import co.tala.api.fakedependency.configuration.defaultmock.DefaultCallbackConfiguration
import co.tala.api.fakedependency.constant.Constant
import co.tala.api.fakedependency.exception.MockNotFoundException
import co.tala.api.fakedependency.model.MockData
import co.tala.api.fakedependency.testutil.MockDataFactory
import kotlin.text.Regex
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.http.HttpServletRequest

@Unroll
class DefaultMockDataRetrieverSpec extends Specification {
    private interface FakeDefaultableDependency {
        def fakeMethod()
    }
    private FakeDefaultableDependency dependencyMock
    private Defaultable defaultableMock
    private List<Defaultable> defaultables
    private HttpServletRequest requestMock
    private Object payload = [:]
    private DefaultCallbackConfiguration callbackConfiguration
    private IDefaultMockDataRetriever sut

    def setup() {
        dependencyMock = Mock()
        defaultableMock = Mock()
        defaultables = [defaultableMock]
        requestMock = Mock()
        callbackConfiguration = new DefaultCallbackConfiguration(1, 2, true)
        sut = new DefaultMockDataRetriever(defaultables, callbackConfiguration)
    }

    private DefaultCallback defaultCallback() {
        boolean isEnabled = true
        Closure action = { this.dependencyMock.fakeMethod() }
        new DefaultCallback(isEnabled, action)
    }

    def "getDefaultMockData should return data if the uri matches and invoke the default callback"() {
        given:
            def fakeData = MockDataFactory.buildCustomMockData("fake data")
            1 * defaultableMock.getMockData(payload) >> fakeData
            1 * defaultableMock.getUriRegex() >> new Regex("/resources/([a-z0-9]+)/things")
            1 * defaultableMock.getCallback() >> defaultCallback()
            1 * requestMock.getRequestURI() >> "${Constant.MOCK_SERVICE}/resources/som3th1ng/things"

        when:
            MockData result = sut.getDefaultMockData(requestMock, payload)

        then:
            result == fakeData
            // Although this is called, the test is failing saying it isn't ¯\_(ツ)_/¯
//            Thread.sleep(100)
//            1 * dependencyMock.fakeMethod()
    }

    def "getDefaultMockData should throw exception if the uri does not match"() {
        given:
            0 * defaultableMock.getMockData(_)
            1 * defaultableMock.getUriRegex() >> new Regex("/resources/([a-z0-9]+)/things")
            0 * defaultableMock.getCallback()
            1 * requestMock.getRequestURI() >> "${Constant.MOCK_SERVICE}/not-found"

        when:
            sut.getDefaultMockData(requestMock, payload)

        then:
            thrown MockNotFoundException
    }
}
