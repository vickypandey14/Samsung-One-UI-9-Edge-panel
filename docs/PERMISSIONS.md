# Permissions

EDGE requests only what this foundation uses.

| Permission | Why |
| --- | --- |
| `SYSTEM_ALERT_WINDOW` | The handle and the panel are `TYPE_APPLICATION_OVERLAY` windows. This is a special permission. The app sends the user to `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` and then reads `Settings.canDrawOverlays`. It is not a runtime dialog permission. |
| `FOREGROUND_SERVICE` | The overlay has to keep running after the activity leaves the foreground. |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Required on API 34+ together with the service type below. |
| `POST_NOTIFICATIONS` | Requested on API 33+ so the status notification is allowed on versions that gate notifications. Denial does not block activation. Foreground-service notifications are a platform requirement and can still be shown when this permission is denied. The activate screen says that. |

Not requested, and not present in the manifest: accessibility, `QUERY_ALL_PACKAGES`, storage, contacts, location, microphone, camera, internet, `VIBRATE`, boot completed, battery-optimization exemption, root, Shizuku, or ADB.

Haptics on long-press use `View.performHapticFeedback`. That follows the system haptic setting and does not need `VIBRATE`.

## Why `specialUse`

Android 14 requires every foreground service to name a type. The published types are camera, microphone, location, media playback, media projection, phone call, data sync, connected device, health, remote messaging, system exempted, and special use.

EDGE does not use any of those device capabilities. Its work is a user-invoked overlay window that stays on screen until the user stops it. `specialUse` is the type Android documents for a valid foreground-service case that the other types do not cover.

The manifest property `android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE` says that in plain language: a user-invoked floating edge panel drawn with `TYPE_APPLICATION_OVERLAY`, kept up until the user stops EDGE, and not using camera, microphone, location, media playback, data sync, phone calls, or connected devices.

`startForeground` passes `FOREGROUND_SERVICE_TYPE_SPECIAL_USE` only on API 34 and newer. On API 29 through 33 the platform does not know that constant, so the service uses the older two-argument `startForeground`. The primary device is API 37, which takes the typed path.

Play Console review of `specialUse` is not part of this task. The subtype string is written for that review anyway.

## Background-start order

From Android 15, an app that holds `SYSTEM_ALERT_WINDOW` may start a foreground service from the background only when a `TYPE_APPLICATION_OVERLAY` window is already visible. EDGE starts the service from the activity, while the app is in the foreground, and calls `startForeground` before adding windows. It does not rely on the older, broader overlay exemption.

## Notification

Channel id `edge_overlay_status`, notification id `71001`. The channel has no sound and no vibration. The notification is ongoing, names EDGE, says the handle is available, and has a **Stop EDGE** action. Stopping removes the notification with `STOP_FOREGROUND_REMOVE` and removes both windows. There is no separate "EDGE stopped" notification.

## Revocation

`AppOpsManager.startWatchingMode(OPSTR_SYSTEM_ALERT_WINDOW)` runs for this package while the service is alive. A change that leaves `Settings.canDrawOverlays` false stops the service and records a permission-revoked notice for the dashboard. The next window operation also treats `SecurityException` as a hard failure instead of ignoring it.
