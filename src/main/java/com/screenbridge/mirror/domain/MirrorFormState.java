package com.screenbridge.mirror.domain;

/**
 * Stores the raw values currently entered in the launcher form.
 */
public record MirrorFormState(
        String scrcpyPath,
        String adbPath,
        DeviceInfo selectedDevice,
        String wifiAddress,
        String maxSize,
        String maxFps,
        String videoBitRate,
        String windowWidth,
        String windowHeight,
        boolean fitWindowToScreen,
        String fitAspectRatio,
        boolean fullscreen,
        boolean alwaysOnTop,
        InputMode keyboardMode,
        InputMode mouseMode,
        String pushTarget,
        boolean noAudio,
        boolean turnScreenOff,
        boolean stayAwake,
        int hostAvailableWidth,
        int hostAvailableHeight) {
}
