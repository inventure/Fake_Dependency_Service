environments {
    charles {
        def localhost = "http://localhost.charlesproxy.com"
        fakeDependencyApiUrl = "$localhost:8099/fake-dependency/api"
        immunizationDeciderApiUrl = "$localhost:9000/immunization-decider"
        profileCreationCompletionNotificationTopic = "local-credit-profile-results"
    }

    local {
        def localhost = "http://localhost"
        fakeDependencyApiUrl = "$localhost:8099/fake-dependency/api"
        immunizationDeciderApiUrl = "$localhost:9000/immunization-decider"
        profileCreationCompletionNotificationTopic = "local-credit-profile-results"
    }
}
