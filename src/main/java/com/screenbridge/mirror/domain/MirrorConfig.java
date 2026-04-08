package com.screenbridge.mirror.domain;

import java.nio.file.Path;

/**
 * Stores the validated configuration required to launch scrcpy.
 */
public record MirrorConfig(
        Path scrcpyPath,
        Path adbPath,
        String deviceSerial,
        String windowTitle,
        Integer maxSize,
        Integer maxFps,
        String videoBitRate,
        Integer windowWidth,
        Integer windowHeight,
        boolean fullscreen,
        boolean alwaysOnTop,
        InputMode keyboardMode,
        InputMode mouseMode,
        String pushTarget,
        boolean noAudio,
        boolean turnScreenOff,
        boolean stayAwake) {
}
