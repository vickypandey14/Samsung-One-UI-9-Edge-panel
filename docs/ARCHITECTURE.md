# Architecture

EDGE is one Android process and one APK. The Gradle project is a single `:app` module because the overlay service, the permission checks, and the Compose UI have to ship together. Splitting them into empty library modules would not change the runtime boundary. The package tree is the boundary:

```text
dev.edgepanel.app
  EdgeApplication, MainActivity, EdgeNavHost
  core.common          logging, dispatchers
  core.designsystem    tokens and Material 3 theme
  core.permissions     overlay permission gate and settings intents
  core.datastore       handle edge, vertical fraction, onboarding flag
  domain               session state machine and display measurements
  overlay.service      foreground service, windows, notification
  overlay.handle       handle geometry and face
  overlay.panel        panel geometry and content
  overlay.gestures     pointer classification and the handle touch binder
  features.onboarding
  features.dashboard
```

## What owns what

The activity never owns an overlay view. `EdgeOverlayService` creates an `OverlayViewHost` that implements `LifecycleOwner`, `SavedStateRegistryOwner`, and `ViewModelStoreOwner`. Both `ComposeView`s install that host before `setContent`. The host is destroyed when the service removes the windows, and its `ViewModelStore` is cleared there. Nothing in the overlay keeps the activity.

`OverlaySessionRepository` is a Hilt singleton because the activity and the service are different components that have to share one session. It is not a Kotlin `object`. The service is the only writer that produces window effects. The UI reads `StateFlow` and starts or stops the service through `EdgeOverlayStarter`.

`OverlayStateMachine` is a pure function from `(state, event)` to `(state, effects)`. The service executes effects: attach the handle, animate the panel, move the handle, refresh after rotation, or detach both windows. Unit tests cover the transitions. They do not need Robolectric.

Preferences use one DataStore file, created once by Hilt. DataStore's own API requires a single instance per file. Handle position is written when a drag ends, not on every pixel. The vertical position is a fraction of the usable band so rotation can reclamp it.

Coroutines started by the service use a `SupervisorJob` cancelled in `onDestroy`. There is no `GlobalScope`.

## Windows

Two `TYPE_APPLICATION_OVERLAY` windows:

- **Handle.** 7dp by 68dp, 12dp corner radius, window alpha 0.85. Default side is the right edge, vertically centered in the band between the status/cutout/gesture top and the navigation/gesture bottom. `LayoutParams` width and height are the handle's real size. The window is `FLAG_NOT_FOCUSABLE` and `FLAG_NOT_TOUCH_MODAL`. It does not watch outside touches.
- **Panel.** 300dp wide, shorter if the screen cannot fit the handle beside it. Height is the safe vertical band. Inner corners are 28dp; the outer edge is square against the screen. The panel is not focusable, not touch-modal, and sets `FLAG_WATCH_OUTSIDE_TOUCH`. An outside touch closes the panel and is still delivered to the app underneath. The panel view is removed when the close animation ends, so no invisible window remains.

There is no full-screen touch window and no `FLAG_DIM_BEHIND`. The visible pill stays 7 by 68dp. On One UI the outer edge is reserved by the back gesture and by the system edge grab, so a pill glued to that edge never receives the tap. The closed handle is therefore placed just inside the side gesture inset (at least 48dp from the edge, and never more than 72dp), and its window is 56dp wide extending inward. Only that strip is excluded from the system gesture. Above it, below it, and on the opposite edge, the system gesture is untouched. Vertical clamping uses the gesture inset, so the handle stays out of the bottom gesture pill and the status bar. A display cutout can push the handle further inward. Both windows set `FLAG_LAYOUT_NO_LIMITS` so the panel can start off-screen and slide in. An outside touch in the first 450ms after open is ignored, because that touch is the finger that opened the panel.

The panel slides for 280ms. Rotation cancels that animation and snaps to the current phase so the session cannot get stuck in Opening or Closing.

## Permission and service lifetime

Onboarding stores "the settings screen was opened" separately from "the platform says overlay is allowed". Only `Settings.canDrawOverlays` moves the flow forward.

The service calls `startForeground` before it reads preferences or adds a window. The type is `specialUse` on API 34+ and the two-argument `startForeground` on older APIs, because `FOREGROUND_SERVICE_TYPE_SPECIAL_USE` was added in API 34. See [PERMISSIONS.md](PERMISSIONS.md).

`START_NOT_STICKY` is intentional. This task does not restart EDGE after reboot or after the process is killed. The return value also avoids a null-intent restart that would try to draw again without the user.

While the service is running it watches `OPSTR_SYSTEM_ALERT_WINDOW`. If the grant disappears, both windows are removed, the notification is removed, and the session records `PermissionRevoked`.

WindowManager failures (`SecurityException`, `BadTokenException`, `IllegalArgumentException`, other `RuntimeException`) are logged with the throwable and then tear the overlay down. `IllegalArgumentException` from removing a view that is already gone is logged and not treated as a second failure.

## UI

Onboarding and the dashboard are Compose screens in a Navigation `NavHost`. The dashboard stores the edge, handle visibility, size, opacity, skin, title, clipboard note, and pinned apps. The service collects those changes and relayouts. The panel opens shortcuts, that note, and launcher apps. Launcher apps are listed through a manifest `queries` intent, not `QUERY_ALL_PACKAGES`. The flashlight uses `CameraManager.setTorchMode` and does not request the camera permission.

Dynamic color is not used. The accent stays the same restrained blue in light and dark so the first build does not depend on the wallpaper.
