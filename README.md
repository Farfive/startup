# Dream App (Working Title)

<!-- A brief 1-2 sentence description of what the app does. Update this later! -->
A mobile application built with Kotlin and Jetpack Compose for [Your App's Purpose].

## Table of Contents

- [Setup](#setup)
- [Configuration](#configuration)
- [Building](#building)
- [Testing](#testing)
- [Architecture](#architecture)
- [CI/CD](#cicd)

## Setup

**Prerequisites:**

*   Android Studio (latest stable version recommended)
*   Java Development Kit (JDK) 17 (as configured in `build.gradle.kts`)
*   Git

**Steps:**

1.  **Clone the repository:**
    ```bash
    git clone <your-repository-url>
    cd Dream
    ```
2.  **Import into Android Studio:**
    *   Open Android Studio.
    *   Select "Open" or "Import Project".
    *   Navigate to the cloned `Dream` directory and select it.
    *   Allow Android Studio to sync Gradle and download dependencies.
3.  **Create Secrets File:**
    *   Create a `gradle.properties` file in the project root directory (the same directory as this README).
    *   Add necessary secret keys (like `DREAM_API_KEY`). See the [Configuration](#configuration) section.
    *   **Important:** This file is listed in `.gitignore` and should **not** be committed to version control.

## Configuration

Application configuration values are managed through Gradle and exposed via the `BuildConfig` class.

*   **Secrets:** Sensitive keys (e.g., API keys) should be placed in the root `gradle.properties` file (which is **not** checked into version control).
    ```properties
    # Example: /gradle.properties
    DREAM_API_KEY="YOUR_SECRET_KEY_HERE"
    # DEBUG_DREAM_API_KEY="YOUR_DEBUG_KEY_HERE" # Optional override for debug builds
    ```
    These are accessed in code via `BuildConfig.DREAM_API_KEY`.
*   **Build-Type Specific Values:** Different values for `debug` and `release` builds (like API base URLs or feature flags) are configured directly in `app/build.gradle.kts` within the `buildTypes` block and accessed via `BuildConfig` (e.g., `BuildConfig.API_BASE_URL`, `BuildConfig.DEBUG_MODE`).
*   **Signing Configuration:** Release signing requires environment variables (`KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) to be set, referencing a keystore file (default path: `../keystore/release-keystore.jks` relative to the `app` module).

## Building

Use the Gradle wrapper (`./gradlew`) from the project root directory to build the application:

*   **Build Debug APK:**
    ```bash
    ./gradlew assembleDebug
    ```
    *(Output: `app/build/outputs/apk/debug/app-debug.apk`)*

*   **Build Release APK:** (Requires signing configuration to be set up)
    ```bash
    ./gradlew assembleRelease
    ```
    *(Output: `app/build/outputs/apk/release/app-release.apk`)*

*   **Build Release App Bundle (AAB):** (Requires signing configuration)
    ```bash
    ./gradlew bundleRelease
    ```
    *(Output: `app/build/outputs/bundle/release/app-release.aab`)*

## Testing

*   **Run Unit Tests:** (Execute on local JVM)
    ```bash
    ./gradlew testDebugUnitTest
    ```
    *(Test reports available in `app/build/reports/tests/testDebugUnitTest/`)*

*   **Run Instrumentation Tests:** (Require a connected Android device or emulator)
    ```bash
    ./gradlew connectedDebugAndroidTest
    ```
    *(Test reports available in `app/build/reports/androidTests/connected/`)*

*   **Run Lint Checks:**
    ```bash
    ./gradlew lintDebug
    ```
    *(Report available in `app/build/reports/lint-results-debug.html`)*

## Architecture

<!-- Describe the main architectural pattern (e.g., MVVM, MVI) and key libraries/components used. Update this section as the app develops. -->
*   **Pattern:** [e.g., MVVM (Model-View-ViewModel)]
*   **UI:** Jetpack Compose
*   **Dependency Injection:** Hilt
*   **Networking:** Retrofit
*   **Database:** Room
*   **Asynchrony:** Kotlin Coroutines & Flows
*   **Logging:** Timber

## CI/CD

Continuous Integration is set up using GitHub Actions (`.github/workflows/android-ci.yml`).

*   **Triggers:** Runs on push/pull request to `main` and `develop` branches.
*   **Checks:** Performs lint checks, runs unit tests, and builds a debug APK.
*   **Status:** [![Android CI](https://github.com/<your-github-username>/<your-repo-name>/actions/workflows/android-ci.yml/badge.svg)](https://github.com/<your-github-username>/<your-repo-name>/actions/workflows/android-ci.yml)
    *(Remember to replace `<your-github-username>/<your-repo-name>` with your actual GitHub details to get the status badge working)* 