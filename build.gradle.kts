plugins {
    id("org.sonarqube") version "7.2.3.7755"
}

allprojects {
    group = "at.se2group"
    version = "0.1.0"
}

sonar {
    properties {
        property("sonar.projectKey", "SE2-Gruppenprojekt_se2-group-codebase")
        property("sonar.organization", "se2-gruppenprojekt")
    }
}

project(":apps:android:app") {
    sonar {
        isSkipProject = true
    }
}
