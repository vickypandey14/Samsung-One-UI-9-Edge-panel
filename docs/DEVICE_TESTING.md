# Device testing

Target: Samsung Galaxy A35 5G, SM-A356E, One UI 9.0 / Android 17.

## Unit tests

Command, run from the project root with JDK 17:

```bat
gradlew.bat :app:testDebugUnitTest
```

Result on this machine, 23 November 2026: **37 tests, 0 failures, 0 errors.**

| Suite | Tests |
| --- | --- |
| Overlay permission evaluation | 3 |
| Onboarding reducer, including "settings opened" without a grant | 4 |
| Overlay session transitions | 13 |
| Handle clamping and edge placement | 5 |
| Panel left/right geometry | 4 |
| Gesture classification | 7 |
| Design-token spec | 1 |

These tests do not need the phone. They cover permission evaluation, session transitions (activate, open, close, reverse mid-animation, stop, permission revocation, fraction clamp), handle bounds, panel placement, and gesture direction.

## On-device checklist

| # | Check | Result |
| --- | --- | --- |
| 1 | Grant overlay permission | Not run. No SM-A356E was attached to this computer (`adb devices` was empty). |
| 2 | Activate EDGE | Not run |
| 3 | Handle appears | Not run |
| 4 | Tap opens, close control closes | Not run |
| 5 | Inward swipe opens | Not run |
| 6 | Vertical drag repositions and stays in bounds | Not run |
| 7 | Overlay remains over another app | Not run |
| 8 | System back gesture still works outside the handle | Not run |
| 9 | Lock and unlock | Not run |
| 10 | Rotate | Not run |
| 11 | Notification Stop removes both windows and the notification | Not run |
| 12 | Revoke overlay permission while EDGE is active | Not run |
| 13 | Start EDGE again after stopping it | Not run |

Samsung-specific notes cannot be recorded until the phone is connected. Logcat tag `EDGE` prints the display size and gesture insets once when the handle is attached.

## What this machine could not prove

A debug APK and passing unit tests do not prove the overlay on One UI 9. The service, `WindowManager` flags, notification, and permission revocation need the checklist above on the SM-A356E.
