# Device testing

Target: One UI 9.0 / Android 17, and later One UI versions that keep the same overlay rules.

## Unit tests

Command, run from the project root with JDK 17:

```bat
gradlew.bat :app:testDebugUnitTest
```

Latest local unit-test run: **41 tests, 0 failures, 0 errors.**

| Suite | Tests |
| --- | --- |
| Overlay permission evaluation | 3 |
| Onboarding reducer, including "settings opened" without a grant | 4 |
| Overlay session transitions | 13 |
| Handle clamping and edge placement | 7 |
| Panel left/right geometry | 4 |
| Gesture classification | 9 |
| Design-token spec | 1 |

These tests do not need the phone. They cover permission evaluation, session transitions (activate, open, close, reverse mid-animation, stop, permission revocation, fraction clamp), handle bounds, panel placement, and gesture direction.

## On-device checklist

| # | Check | Result |
| --- | --- | --- |
| 1 | Grant overlay permission | Not run |
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

One UI behavior still needs the checklist above on a phone. Logcat tag `EDGE` prints the display size and gesture insets once when the handle is attached.

## What unit tests cannot prove

A debug APK and passing unit tests do not prove the overlay on One UI 9. The service, `WindowManager` flags, notification, and permission revocation need the checklist above on a One UI device.
