# Versions

Selected on 23 November 2026. All of these are stable releases. No alpha or beta libraries are on the compile or runtime classpath.

| Component | Version | Why this one |
| --- | --- | --- |
| Android Gradle Plugin | 9.5.2 | Current stable. Maximum API level is 37. Requires Gradle 9.6.0 and JDK 17. Built-in Kotlin is on, so the project does not apply `org.jetbrains.kotlin.android`. |
| Gradle | 9.6.0 | Minimum and default Gradle for AGP 9.5. |
| JDK | 17 | Required by AGP 9.5. |
| Kotlin | 2.4.21 | Current stable. The Compose compiler plugin uses the same version. |
| KSP | 2.3.14 | Current stable. Supports Kotlin 2.3.20 through 2.4.x and AGP 9. |
| Hilt / Dagger | 2.60.1 | Current stable with KSP support and AGP 9 support. |
| AndroidX Hilt | 1.4.0 | Stable `hiltViewModel()` for Compose, in `androidx.hilt:hilt-lifecycle-viewmodel-compose`. |
| Compose BOM | 2026.11.01 | Current stable. Maps Compose UI to 1.13.0 and Material 3 to 1.5.0. Requires `compileSdk` 37.1 and Kotlin 2.2 or newer. |
| AndroidX Core | 1.19.1 | Current stable. |
| AndroidX Lifecycle | 2.11.1 | Current stable. |
| AndroidX Activity | 1.13.0 | Current stable `activity-compose`. |
| Navigation Compose | 2.10.2 | Current stable. |
| DataStore Preferences | 1.3.0 | Current stable. |
| SavedState | 1.3.1 | Explicit dependency so overlay `ComposeView`s can install a `SavedStateRegistryOwner`. Gradle may upgrade it if a newer AndroidX artifact requires that. |
| Coroutines | 1.11.0 | Current stable. |
| JUnit | 4.13.2 | Unit tests for the pure session, geometry, gesture, and permission rules. |

`minSdk` is 29. `targetSdk` is 37. `compileSdk` is 37.1.

IDE: Android Studio Rabbit 2 (2026.2.2) or any newer studio that bundles AGP 9.5.
