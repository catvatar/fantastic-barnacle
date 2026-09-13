# Repository guide

## Project

100 Steps — a minimal Android step counter (Kotlin + Jetpack Compose).

- App module: `app/`, namespace `com.example.stepcounter`
- Pure counting logic in `StepCounterLogic.kt` (UI + tests share it)
- State via `StepCounterViewModel` (StateFlow); sensor wired in `MainActivity`
- Custom `ui/MilestoneProgressBar.kt` draws the 100-step bar with 10/20/50 markers

## Build

- JDK 17 and Android SDK (compileSdk/targetSdk 35, minSdk 26) are required.
  Point AGP at the SDK with a `local.properties` (`sdk.dir=...`) or `ANDROID_HOME`.
- Wrapper: Gradle 8.9 (`./gradlew`).
- Commands:
  - `./gradlew assembleDebug` — build debug APK
  - `./gradlew testDebugUnitTest` — unit tests (10 tests, pure JVM, no mocks)
  - `./gradlew build` — full build including Android Lint

## CI

`.github/workflows/build.yml` builds the debug APK and uploads it as an
artifact on every push/PR; the Actions artifact is the download source of truth.

## House rules

- Keep counting logic pure and in `StepCounterLogic.kt` so it is unit-testable.
- Don't commit `local.properties`, `.kotlin/`, or `app/build/`.
- Version bumps: update `versionCode`/`versionName` in `app/build.gradle.kts`.