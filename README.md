This is a Kotlin Multiplatform project targeting Android, iOS, Desktop (JVM).

* [/dormnet-shared](./dormnet-shared/src) is for shared Compose Multiplatform UI and common Kotlin code.
  It contains platform source sets such as `commonMain`, `androidMain`, `iosMain`, and `jvmMain`.
* [/dormnet-android](./dormnet-android/src) is the Android application module.
* [/dormnet-desktop](./dormnet-desktop/src) is the Desktop application module.
* [/iosApp](./iosApp) is the iOS application shell that embeds the `DormNetShared` Kotlin framework.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :dormnet-android:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :dormnet-android:assembleDebug
  ```

### Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :dormnet-desktop:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :dormnet-desktop:run
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
