# EDGE

EDGE is an open-source edge panel for Android. A handle stays on the side of the screen over other apps. Swipe it inward, or tap it, and a panel opens with shortcuts, a clipboard note, and the apps you pin.

Package: `dev.edgepanel.app`

It targets One UI 9.0 on Android 17 (API 37), and later One UI versions that keep the same overlay rules. `minSdk` is 29.

## What it does

- Draws a handle and a panel with `TYPE_APPLICATION_OVERLAY`. There is no full-screen touch blocker.
- Opens from a tap or a short inward swipe. Drag the handle up or down to move it.
- Keeps running from a foreground-service notification until you stop it. It does not start on boot.
- Lets you hide the handle, change its length and opacity, pick the left or right edge, and resize the panel.
- Styles the panel: Classic, Bubblegum, Midnight, Terminal, or Party, plus a title and optional lines.
- Controls panel glass: depth, blur, or a solid panel with transparency off.
- Shows the shortcuts you turn on: light, Wi-Fi, Bluetooth, display, sound, location, battery, alerts, clock, settings, airplane mode, data use, wallpaper, camera, phone, messages, and flipping the edge.
- Keeps a note in the panel. Copy writes it to the clipboard. Read fills it from the clipboard.
- Lists launcher apps and lets you pin up to 16 of them into the panel.

The in-app screen has four pages: **Home**, **Look**, **Style**, and **Apps**.

## Requirements

- JDK 17
- Android SDK platform **37.1** (required by Compose BOM 2026.11.01; `targetSdk` stays 37)
- Android SDK Build-Tools 37.0.0
- An IDE that supports Android Gradle Plugin 9.5

`compileSdk` is 37.1 because that Compose BOM does not compile against platform 37.0. The app still targets API 37.

Pinned versions are in [docs/VERSIONS.md](docs/VERSIONS.md).

## Build

From the project root, set `JAVA_HOME` to JDK 17 and put the SDK path in `local.properties`:

```properties
sdk.dir=C:\\path\\to\\Android\\Sdk
```

`local.properties` is gitignored.

PowerShell does not run scripts in the current folder unless you prefix `.\`:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

Command Prompt:

```bat
gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

Debug APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Install

`adb` is in the SDK `platform-tools` directory. It is often not on `PATH`.

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
```

Use your own SDK path if it is different.

## First run

1. Open EDGE and finish the five onboarding steps.
2. Turn on **Allow display over other apps** for EDGE. Opening that screen is not a grant. EDGE reads `Settings.canDrawOverlays` when you come back.
3. Activate EDGE. A notification stays up while the overlay is running.
4. Leave the app. The handle sits on the edge you chose.
5. Tap the handle or swipe it inward. Close the panel with Close, another tap on the handle, or a tap outside it.
6. Long-press or drag the handle vertically to move it.
7. Stop EDGE from the notification or from Home. Both windows and the notification go away.

## Permissions

EDGE asks only for overlay drawing, a special-use foreground service, and notifications. It does not ask for accessibility, `QUERY_ALL_PACKAGES`, storage, contacts, location, microphone, camera, or internet. Launcher apps are listed with a `<queries>` intent. The flashlight uses `CameraManager.setTorchMode` and does not request the camera permission.

Details are in [docs/PERMISSIONS.md](docs/PERMISSIONS.md).

## Project layout

One Gradle module, `:app`. Packages are `core`, `domain`, `overlay`, and `features`. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Docs

- [Architecture](docs/ARCHITECTURE.md)
- [Permissions](docs/PERMISSIONS.md)
- [Versions](docs/VERSIONS.md)
- [Device testing](docs/DEVICE_TESTING.md)
- [Known limitations](docs/KNOWN_LIMITATIONS.md)

No license file is included yet. Add one before publishing the repository.
