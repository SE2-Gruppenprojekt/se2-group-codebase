rootProject.name = "se2-group-codebase"

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
/*
   Docs: https://docs.gradle.org/current/userguide/declaring_dependencies_basics.html
   TL;DR should make project accessors less error prone and improve consistency
*/
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":apps:android:app")
include(":apps:backend")
include(":apps:shared")

project(":apps:android:app").projectDir = file("apps/android/app")
project(":apps:backend").projectDir = file("apps/backend")
project(":apps:shared").projectDir = file("apps/shared")
