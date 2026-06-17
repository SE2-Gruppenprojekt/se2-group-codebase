plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    id("org.sonarqube")
    jacoco
}

java {
    toolchain {
        languageVersion.set(
            JavaLanguageVersion.of(
                libs.versions.jvmTarget.get().toInt()
            )
        )
    }
}

kotlin {
    jvmToolchain(
        libs.versions.jvmTarget.get().toInt()
    )
}


dependencies {
    implementation(projects.apps.shared)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.8.5")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.auth0:java-jwt:4.4.0")


    testImplementation(libs.spring.boot.starter.test)
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    classDirectories.setFrom(
        files(
            classDirectories.files.map { classesDir ->
                fileTree(classesDir) {
                    // Ignore stale duplicate class artifacts such as "Foo 2.class" that can
                    // appear in local incremental output and break JaCoCo bundle creation.
                    exclude("**/* 2.class")
                }
            }
        )
    )
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

sonar {
    properties {
        property("sonar.projectKey", "SE2-Gruppenprojekt_se2-group-codebase")
        property("sonar.organization", "se2-gruppenprojekt")
        property("sonar.sources", "src/main/kotlin")
        property("sonar.tests", "src/test/kotlin")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "build/reports/jacoco/test/jacocoTestReport.xml"
        )
    }
}

dependencies {
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
}
