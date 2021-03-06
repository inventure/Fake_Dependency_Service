environments {
    charles {
        def localhost = "http://localhost.charlesproxy.com"
        fakeDependencyApiUrl = "$localhost:8099/mock-service"
        immunizationDeciderApiUrl = "$localhost:9000/immunization-decider"
    }

    local {
        def localhost = "http://localhost"
        fakeDependencyApiUrl = "$localhost:8099/mock-service"
        immunizationDeciderApiUrl = "$localhost:9000/immunization-decider"
    }
}
