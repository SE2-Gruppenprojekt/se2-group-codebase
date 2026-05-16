import org.gradle.internal.declarativedsl.language.FunctionArgument

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("org.sonarqube")
    alias(libs.plugins.protobuf)
    jacoco
}

jacoco {
    toolVersion = "0.8.11"
}


android {
    namespace = "at.aau.serg.android"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "at.aau.serg.android"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        testOptions {
            unitTests {
                isIncludeAndroidResources = true
            }
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
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

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
    }
    generateProtoTasks {
        all().forEach {
            it.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}



tasks.register<JacocoReport>("jacocoTestReport") {

    dependsOn("testReleaseUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)

        xml.outputLocation.set(
            layout.buildDirectory.file("reports/jacoco/jacoco.xml")
        )
    }

    val excludes = listOf(
        // androidTest filter
        "**/ComposableSingletons*.*",
        "**/MainActivity.*",
        "**/MainActivityKt.class",
        "**/*Screen.kt",
        "**/*Screen*.kt",
        "**/*ScreenKt.class",
        "**/*TestTags.*",
        "**/components/**",
        "**/navigation/**",
        "**/theme/**",

        // Compose compiler generated
        "**/ComposableSingletons*.*",
        "**/*inlined*.*",
        "**/*lambda*.*",

        // Protocol Buffers Generated Files
        "**/at/aau/serg/android/datastore/proto/**",
        "**/*OuterClass*",
        "**/*ProtoDataStore*",
        "**/*\$Builder*",
        "**/*\$OrBuilder*",

        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*"
    )

    val kotlinClasses = fileTree(
        "${layout.buildDirectory.get()}/tmp/kotlin-classes/release"
    ) {
        exclude(excludes)
    }

    val javaClasses = fileTree(
        "${layout.buildDirectory.get()}/intermediates/javac/release"
    ) {
        exclude(excludes)
    }

    classDirectories.setFrom(
        files(kotlinClasses, javaClasses)
    )

    sourceDirectories.setFrom(
        files(
            "src/main/java",
            "src/main/kotlin"
        )
    )

    executionData.setFrom(
        fileTree(layout.buildDirectory.get()) {
            include(
                "jacoco/testReleaseUnitTest.exec"
            )
        }
    )
}

sonar {
    properties {
        property("sonar.projectKey", "se2-gruppenprojekt_se2-group-codebase_frontend")
        property("sonar.organization", "se2-gruppenprojekt")
        property("sonar.sources", "src/main/java")
        property("sonar.tests", "src/test/java, src/androidTest/java")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "build/reports/jacoco/jacoco.xml"
        )
    }
}

dependencies {
    // Project module dependencies
    implementation(projects.apps.shared)

    // Core Android + Kotlin
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)

    // Jetpack Compose (UI Layer)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.material.icons.extended)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime)
    implementation(libs.reorderable)

    // Navigation (App structure)
    implementation(libs.navigation.compose)

    // Networking (Backend communication)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)

    // Real-time communication (WebSockets / STOMP)
    implementation(libs.krossbow.stomp.core)
    implementation(libs.krossbow.websocket.okhttp)

    // Serialization
    implementation(libs.kotlinx.serialization.json)


    // Local storage (DataStore / Protobuf)
    implementation(libs.datastore.preferences)
    implementation(libs.datastore.core)
    implementation(libs.protobuf.javalite)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.animation.core)
    implementation(libs.androidx.foundation)

    // Unit testing
    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.turbine)

    // Android UI Testing (instrumentation)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.mockk.android)

    // Debug-only tools
    debugImplementation(libs.compose.tooling)
    debugImplementation(libs.compose.test.manifest)
    testImplementation(kotlin("test"))
}
