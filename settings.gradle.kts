rootProject.name = "Fake-Dependency-Service"
include("fake-dependency-service")
include("example-service-under-test")
project(":example-service-under-test").projectDir = file("example-service-under-test")

