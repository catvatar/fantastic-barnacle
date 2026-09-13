# 100 Steps

A minimal Android step-counter. One screen, a 100-step progress bar with
milestones at **10 · 20 · 50**, and two buttons for desk testing.

- Steps are counted only while the app is open, via the hardware
  `TYPE_STEP_COUNTER` sensor.
- **+1 Step** adds a manual step (no need to move).
- **Reset** re-baselines the sensor and clears the count.

## Requirements

- JDK 17
- Android SDK (compile against `android-35`; minSdk 26, targetSdk 35)
- Gradle 8.9 (the wrapper is included)

## Build

```sh
./gradlew assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/app-debug.apk`.

## Test

```sh
./gradlew testDebugUnitTest
```

Runs 14 pure-JVM unit tests (count derivation, sensor baseline/delta,
progress fraction, milestone reaching) with no mocks or fixtures.

## CI

GitHub Actions builds the debug APK and uploads it as an artifact on every
push and PR. See `.github/workflows/build.yml`. The artifact can be
downloaded from the workflow run page and sideloaded onto a device.
