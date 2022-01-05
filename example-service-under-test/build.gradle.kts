import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.2.13.RELEASE"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.spring") version "1.6.0"
    id("org.jetbrains.kotlin.plugin.jpa") version "1.6.0"
    groovy
    java
    jacoco
}

group = "co.tala.api.immunization_decider"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

val kotestVersion = "5.0.3"
val mockkVersion = "1.12.1"

//configurations {
//    compileOnly {
//        extendsFrom(configurations.annotationProcessor.get())
//    }
//}

repositories {
    mavenCentral()
}

dependencies {
//    implementation("org.springframework.boot:spring-boot-starter-data-redis")
//    implementation("org.springframework.session:spring-session-data-redis")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("mysql:mysql-connector-java:8.0.27")
    implementation("io.springfox:springfox-swagger2:2.6.1")
    implementation("org.apache.commons:commons-pool2:2.11.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.8.5")
    implementation("io.springfox:springfox-swagger-ui:2.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.google.code.gson:gson:+")
    implementation("io.kotest:kotest-assertions-core:$kotestVersion")
    implementation("io.kotest:kotest-property:$kotestVersion")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    maxParallelForks = 4

    systemProperty("environment", System.getProperty("environment"))
}

//tasks.test {
//    extensions.configure(JacocoTaskExtension::class) {
//        setDestinationFile(file("$buildDir/jacoco/jacoco.exec"))
//    }
//    finalizedBy("jacocoTestReport")
//}
//
//tasks.jacocoTestReport {
//    reports {
//        html.required.set(true)
//        html.outputLocation.set(file("${buildDir}/reports/coverage"))
//        xml.required.set(false)
//        csv.required.set(false)
//    }
//
//    finalizedBy("jacocoTestCoverageVerification")
//}
//
//tasks.jacocoTestCoverageVerification {
//    violationRules {
//        rule {
//            limit {
//                counter = "LINE"
//                value = "COVEREDRATIO"
//                minimum = "0.01".toBigDecimal()
//            }
//        }
//
//        classDirectories.setFrom(
//            sourceSets.main.get().output.asFileTree.matching {
//                exclude("co/tala/api/TODO/application/**")
//            }
//        )
//    }
//}
