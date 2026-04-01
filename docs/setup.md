# Setup

## Overview

This document explains how to set up the project locally for development.

## Prerequisites

Make sure the following tools are installed:

- Git
- JDK 17
- Android Studio
- IntelliJ IDEA (optional, useful for backend work)

## Clone the Repository

```bash
git clone <repository-url>
cd <repository-folder>#
```

Gradle

This project uses the Gradle Wrapper.
Always use the wrapper instead of a globally installed Gradle version.

**MacOs / Linux**

```bash
./gradlew tasks
```

**Windows**
`

```bat
./gradlew tasks
```

---

Android App

To run the Android frontend: 1. Open the repository in Android Studio 2. Wait for Gradle sync to finish 3. Select the Android run configuration 4. Start an emulator or connect a device 5. Run the app

Backend

To run the backend from the repository root:

```bash
./gradlew :apps:backend:bootRun
```

The backend should start with the local configuration from application.yml.

Configuration Notes
• local IDE files must not be committed
• secrets or private keys must not be committed
• use repository-level configuration and shared conventions where possible

Troubleshooting

Gradle sync fails
• check that JDK 17 is selected
• ensure Gradle wrapper files are present
• refresh Gradle project in the IDE

Android app does not run
• verify emulator/device setup
• check SDK installation in Android Studio

Backend does not start
• check terminal output for configuration errors
• verify module path and Gradle setup
