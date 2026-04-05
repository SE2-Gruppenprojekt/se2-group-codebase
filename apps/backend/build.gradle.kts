plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
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
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")


    testImplementation(libs.spring.boot.starter.test)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
