plugins {
    alias(libs.plugins.kotlin.jvm)
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
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
