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
    compilerOptions {
        jvmTarget.set(
            org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(
                libs.versions.jvmTarget.get()
            )
        )
    }
}

dependencies {
    implementation(kotlin("stdlib"))
}
