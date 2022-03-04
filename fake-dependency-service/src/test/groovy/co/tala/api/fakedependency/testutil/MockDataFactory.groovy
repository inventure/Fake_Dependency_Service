package co.tala.api.fakedependency.testutil

import co.tala.api.fakedependency.model.MockData
import co.tala.api.fakedependency.model.ResponseSetUpMetadata


class MockDataFactory {
    static MockData buildDefaultMockData() {
        buildCustomMockData([name: 'Elliot Masor', id: 123.4])
    }

    static MockData buildCustomMockData(Object data) {
        int httpStatus = 200
        long delayMs = 1
        ResponseSetUpMetadata responseSetUpMetadata = new ResponseSetUpMetadata(httpStatus, delayMs)
        def responseHeaders = ["X-Some-Header": ["some value"]]
        new MockData(data, responseSetUpMetadata, responseHeaders)
    }
}
