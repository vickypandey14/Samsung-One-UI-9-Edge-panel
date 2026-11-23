# Known limitations

- No boot receiver. After reboot, or after the process is killed, the user starts EDGE again from the app. The service returns `START_NOT_STICKY` on purpose.
- The visible handle is still 7 by 68dp, but it sits 20 to 48dp in from the physical edge and the touch window is 48dp wide. On One UI, a pill glued to the bezel is consumed by the system back gesture and by the system edge grab, so the tap never arrives. The rest of the edge is left to the system.
- The panel is not focusable, so the system Back gesture and the Back key are not captured to close it. Close the panel with the button, the handle, or a tap outside it.
- The outside tap is delivered to the app underneath as well as to EDGE. That is what `FLAG_NOT_TOUCH_MODAL` plus `FLAG_WATCH_OUTSIDE_TOUCH` do. The first outside tap dismisses the panel and can also activate whatever was under the finger.
- Elevation shadows are clipped by the overlay window. The panel uses a solid surface and the handle uses a hairline, instead of a larger invisible window just to show a shadow.
- One UI may hide overlays on the secure lock screen and show them again after unlock. That is platform behavior. EDGE does not request show-when-locked.
- One UI battery management can stop a foreground service. EDGE does not request an ignore-battery-optimization exemption. If the handle disappears while the phone is idle, check One UI's battery settings for EDGE.
- API 29 has a coarser inset fallback when `WindowMetrics` is unavailable. One UI 9 runs on API 37 and uses `WindowMetrics` plus the view's applied insets.
- The panel can open Wi-Fi, Bluetooth, display, and sound settings, toggle the flashlight when the device exposes one, keep a note, and launch apps that publish a launcher icon. It does not read notifications, messages, or private app data. One UI only gives clipboard contents to the focused app, so Read focuses the panel for that moment and may still come back empty.
- There is no license file yet.
- On-device results are recorded in [DEVICE_TESTING.md](DEVICE_TESTING.md). Anything not marked there is still untested on a phone.
