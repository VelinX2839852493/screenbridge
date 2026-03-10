# ScreenBridge

ScreenBridge is a Windows desktop launcher for Android device mirroring built on
top of `adb` and `scrcpy`.

It wraps the common mirroring workflow into a Swing UI so users can discover
devices, connect over Wi-Fi, start mirroring, and manage common `scrcpy`
options without typing commands manually.

## Features

- Detects bundled `scrcpy.exe` and `adb.exe` when they are placed in the local
  packaging directory
- Allows manual selection of `scrcpy.exe` and `adb.exe` if they are not bundled
- Lists connected Android devices with parsed status information
- Supports `adb connect` for wireless debugging workflows
- Exposes common scrcpy options such as max size, max FPS, bitrate, and audio
- Provides Chinese and English UI text

## Platform and requirements

- Target platform: Windows
- JDK: 21
- Build tool: Maven 3.9+
- Runtime dependency: Android device with USB debugging enabled

## Run from source

If you already have `scrcpy.exe` and `adb.exe` installed elsewhere, you can run
the app and select them manually in the UI.

```powershell
mvn test
mvn clean package
java -jar target/ScreenBridge.jar
```

## Prepare local packaging assets

This repository does not commit the Windows `scrcpy` bundle under
`src/main/resources/packaging/`. Those files are third-party binaries and should
be downloaded separately before building a distributable package.

Place the extracted official Windows `scrcpy` release under:

```text
src/main/resources/packaging/
  scrcpy-win64-vX.Y/
    scrcpy.exe
    adb.exe
    scrcpy-server
    SDL2.dll
    ...
```

Notes:

- Keep the extracted folder name starting with `scrcpy` so auto-detection works.
- Preserve the original Windows release contents, including the required DLLs.
- Review `THIRD_PARTY_NOTICES.md` before distributing builds that include these
  binaries.

## Build a Windows app image

After preparing `src/main/resources/packaging/`, build the packaged desktop app
with:

```powershell
mvn clean verify
```

`JAVA_HOME` must point to a JDK that includes `jpackage`.

## Project layout

- `src/main/java/com/screenbridge/App.java`: application entry point
- `src/main/java/com/screenbridge/mirror/application`: controller and
  application services
- `src/main/java/com/screenbridge/mirror/domain`: device, request, and event
  models
- `src/main/java/com/screenbridge/mirror/infrastructure`: ADB/scrcpy
  integration and process execution
- `src/main/java/com/screenbridge/mirror/ui`: Swing UI
- `src/main/java/com/screenbridge/mirror/i18n`: language selection and message
  lookup
- `src/main/java/com/screenbridge/mirror/settings`: persisted user settings
- `src/main/resources`: i18n resources and optional local packaging assets
- `src/test/java`: test sources

## License

- Source code in this repository is licensed under MIT. See `LICENSE`.
- Third-party binaries are not stored in this repository by default. See
  `THIRD_PARTY_NOTICES.md` before publishing bundled distributions.
