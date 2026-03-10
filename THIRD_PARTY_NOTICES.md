# Third-Party Notices

This repository contains the ScreenBridge source code.

The Git repository does not include bundled third-party Windows packaging
binaries by default. If you prepare a local packaging directory under
`src/main/resources/packaging/` and publish a packaged Windows build, that
distribution may include third-party components that require separate notice and
license review.

## ScreenBridge source code

- License: MIT
- Scope: Source files in this repository unless stated otherwise

## Optional Windows packaging components

For local packaging, ScreenBridge can use an official Windows `scrcpy` bundle
placed under a directory similar to:

`src/main/resources/packaging/scrcpy-win64-vX.Y/`

That local packaging directory may include third-party executables and runtime
libraries such as the following.

### scrcpy

- Project: scrcpy
- Upstream: https://github.com/Genymobile/scrcpy
- Notes: ScreenBridge uses the upstream Windows bundle for device mirroring.

### Android Debug Bridge and Windows USB libraries

- Components:
  - `adb.exe`
  - `AdbWinApi.dll`
  - `AdbWinUsbApi.dll`
- Upstream: Android SDK Platform-Tools / Android Open Source Project
- Notes: Review the Android SDK and platform-tools redistribution terms before
  publishing binary releases that include these files.

### FFmpeg runtime libraries

- Components:
  - `avcodec-61.dll`
  - `avformat-61.dll`
  - `avutil-59.dll`
  - `swresample-5.dll`
- Upstream: FFmpeg
- Notes: These binaries are shipped as part of the upstream scrcpy Windows
  bundle. Redistributors should preserve the corresponding FFmpeg license
  notices supplied by upstream.

### SDL

- Component:
  - `SDL2.dll`
- Upstream: Simple DirectMedia Layer

### libusb

- Component:
  - `libusb-1.0.dll`
- Upstream: libusb

## Distribution guidance

- Keep this notice file and the repository `LICENSE` file with any packaged
  distribution of ScreenBridge.
- If you publish binary releases, also include the upstream license texts and
  notices for the bundled third-party components.
- The repository source alone is not intended to imply redistribution rights for
  third-party binaries; verify the upstream terms for the exact bundle you use.
- If you replace the bundled Windows package with a different upstream build,
  review and update this file before release.
