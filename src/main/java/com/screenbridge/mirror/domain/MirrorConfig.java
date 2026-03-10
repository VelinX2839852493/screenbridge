package com.screenbridge.mirror.domain;

import java.nio.file.Path;

public record MirrorConfig(
        Path scrcpyPath,
        Path adbPath,
        String deviceSerial,
        String windowTitle,
        Integer maxSize,
        Integer maxFps,
        String videoBitRate,
        boolean noAudio,
        boolean turnScreenOff,
        boolean stayAwake) {
}
