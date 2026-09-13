# Repository guide

## Project

100 Steps — a minimal Android step counter (Kotlin + Jetpack Compose).

- App module: `app/`, namespace `com.example.stepcounter`
- Honest counting rules in `StepCounterLogic.kt` (UI + tests share it)
- State via `StepCounterViewModel` (StateFlow); sensor wired in `MainActivity`
- Custom `ui/MilestoneProgressBar.kt` draws the progress bar with milestone markers

## Build

- JDK 17 and Android SDK (compileSdk/targetSdk 35, minSdk 26) are required.
  Point AGP at the SDK with a `local.properties` (`sdk.dir=...`) or `ANDROID_HOME`.
- Wrapper: Gradle 8.9 (`./gradlew`).
- Commands:
  - `./gradlew assembleDebug` — build debug APK
  - `./gradlew testDebugUnitTest` — unit tests (14 tests, pure JVM, no mocks)
  - `./gradlew build` — full build including Android Lint

## CI

`.github/workflows/build.yml` builds the debug APK and uploads it as an
artifact on every push/PR; the Actions artifact is the download source of truth.

## House rules

### Honest Functions (mandatory)

An **honest function** communicates its complete contract—all inputs accessed
and all outputs produced or modified—strictly through its function signature.

- **It IS**: a function whose behavior is fully controlled by its explicit
  arguments. If it reads data, that data comes from an argument. If it
  modifies state, it only mutates memory explicitly passed to it by the caller
  with permission to modify (e.g., in-place sorting).
- **It IS NOT**: a function that reads from or writes to hidden global state,
  static singletons, system clocks, environment variables, or ambient random
  number generators.
- **Honest vs. Pure**: pure functions ban all mutation. Honest functions
  permit mutation *iff* the target memory is explicitly supplied via the
  parameter list.

Placement in the call tree:

- **Honest leaves**: domain rules, data transformations, and business logic
  go exclusively at the leaves, in pure functions with every input passed by
  parameter. In this repo that's `StepCounterLogic`.
- **Dishonest roots (the "skin")**: system interactions (I/O, rendering,
  network, clock reads) are inherently dishonest. Push them to root nodes:
  entry points, controllers, framework hooks (Activities, ViewModels,
  Composables).
- **Dishonesty is infectious**: calling a dishonest function makes the caller
  dishonest. An honest leaf must never call a dishonest function. Keep
  computation out of the shell and pass values down.

Concrete directives for all agents:

- **Inject dependencies explicitly**: if a function needs time, random values,
  or configuration, take them as parameters rather than reading them
  internally (no `System.currentTimeMillis()`, no `Random()`, no global `Logger`
  demands, no ambient config object reads inside a leaf).
- **Separate computation from side effects**: compute data structures in the
  honest core, then execute side effects from the shell using that data.
- **Avoid ambient reads**: never read from global or static scope within
  domain logic. No `StepCounterLogic.MILESTONES`-style globals to derive with;
  pass the configuration in.
- **Android specifics**: never instantiate `SensorManager` or read
  `MaterialTheme`/`stringResource` inside a leaf. Composables are roots by
  definition — declare their inputs as parameters and derive geometry with
  honest functions.

### Other conventions

- Keep counting logic pure and in `StepCounterLogic.kt` so it is unit-testable.
- Don't commit `local.properties`, `.kotlin/`, or `app/build/`.
- Version bumps: update `versionCode`/`versionName` in `app/build.gradle.kts`.