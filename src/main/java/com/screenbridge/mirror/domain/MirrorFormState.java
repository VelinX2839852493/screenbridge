package com.screenbridge.mirror.domain;

public record MirrorFormState(
        String scrcpyPath,
        String adbPath,
        DeviceInfo selectedDevice,
        String wifiAddress,
        String maxSize,
        String maxFps,
        String videoBitRate,
        boolean noAudio,
        boolean turnScreenOff,
        boolean stayAwake) {
}
