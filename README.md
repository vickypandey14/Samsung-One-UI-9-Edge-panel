# EDGE

Independent, open-source edge panel for Android. This repository is the first foundation: a floating handle and overlay panel that stay available while other apps are open.

Package: `dev.edgepanel.app`

Primary device target: Samsung Galaxy A35 5G (SM-A356E), One UI 9.0 / Android 17 (API 37).

## Requirements

- JDK 17
- Android SDK platform **37.1** (required by Compose BOM 2026.11.01; `targetSdk` stays 37)
- Android SDK Build-Tools 37.0.0
- Android Studio Rabbit 2 (2026.2.2) or another IDE that supports Android Gradle Plugin 9.5

`compileSdk` is 37.1 rather than the literal 37 because the stable November 2026 Compose BOM does not compile against 37.0. The app still targets API 37.

Pinned versions are listed in [docs/VERSIONS.md](docs/VERSIONS.md).

## Build

From the project root, with `JAVA_HOME` pointing at JDK 17 and `local.properties` containing `sdk.dir`:

```bat
gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

Debug APK:

```text
app\build\outputs\apk\debug\app-debug.apk
```

## Install

```bat
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## First run

1. Open EDGE and follow the five onboarding steps.
2. Turn on **Allow display over other apps** for EDGE. Opening that screen is not treated as a grant. The app reads `Settings.canDrawOverlays` when you return.
3. Activate EDGE. A notification stays visible while the overlay is running.
4. Leave the app. The handle is on the right edge (or the left edge, if you switch it on the dashboard).
5. Tap the handle, or swipe it inward, to open the panel. Tap Close, tap the handle, or tap outside the panel to close it.
6. Long-press or drag the handle vertically to move it.
7. Stop EDGE from the notification action or the dashboard. Both overlay windows are removed.

## Modules

One Gradle module, `:app`. Packages follow the requested layout (`core`, `domain`, `overlay`, `features`). Extra Gradle modules would only wrap code that ships in the same process and the same APK. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Docs

- [Architecture](docs/ARCHITECTURE.md)
- [Permissions](docs/PERMISSIONS.md)
- [Versions](docs/VERSIONS.md)
- [Device testing](docs/DEVICE_TESTING.md)
- [Known limitations](docs/KNOWN_LIMITATIONS.md)

No license file is included yet. Add one before publishing the repository.
