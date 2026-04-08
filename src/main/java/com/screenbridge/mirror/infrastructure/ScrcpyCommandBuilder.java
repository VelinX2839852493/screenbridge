package com.screenbridge.mirror.infrastructure;

import com.screenbridge.mirror.domain.MirrorConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the scrcpy startup command from a validated config.
 */
public final class ScrcpyCommandBuilder {
    private ScrcpyCommandBuilder() {
    }

    public static List<String> build(MirrorConfig config) {
        List<String> command = new ArrayList<>();
        command.add(config.scrcpyPath().toString());

        if (isNotBlank(config.deviceSerial())) {
            command.add("--serial=" + config.deviceSerial());
        }
        if (isNotBlank(config.windowTitle())) {
            command.add("--window-title=" + config.windowTitle());
        }

        if (config.maxSize() != null) {
            command.add("--max-size=" + config.maxSize());
        }
        if (config.maxFps() != null) {
            command.add("--max-fps=" + config.maxFps());
        }
        if (isNotBlank(config.videoBitRate())) {
            command.add("--video-bit-rate=" + config.videoBitRate().trim());
        }
        if (config.windowWidth() != null) {
            command.add("--window-width=" + config.windowWidth());
        }
        if (config.windowHeight() != null) {
            command.add("--window-height=" + config.windowHeight());
        }
        if (config.fullscreen()) {
            command.add("--fullscreen");
        }
        if (config.alwaysOnTop()) {
            command.add("--always-on-top");
        }
        if (config.keyboardMode() != null && config.keyboardMode().usesCliValue()) {
            command.add("--keyboard=" + config.keyboardMode().cliValue());
        }
        if (config.mouseMode() != null && config.mouseMode().usesCliValue()) {
            command.add("--mouse=" + config.mouseMode().cliValue());
        }
        if (isNotBlank(config.pushTarget())) {
            command.add("--push-target=" + config.pushTarget().trim());
        }
        if (config.noAudio()) {
            command.add("--no-audio");
        }
        if (config.turnScreenOff()) {
            command.add("--turn-screen-off");
        }
        if (config.stayAwake()) {
            command.add("--stay-awake");
        }

        return command;
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
