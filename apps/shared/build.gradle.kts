plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
     toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(kotlin("stdlib"))
}
