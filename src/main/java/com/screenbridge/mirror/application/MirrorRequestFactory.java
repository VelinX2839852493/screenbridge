package com.screenbridge.mirror.application;

import com.screenbridge.mirror.domain.DeviceInfo;
import com.screenbridge.mirror.domain.MirrorConfig;
import com.screenbridge.mirror.domain.MirrorFormState;
import com.screenbridge.mirror.domain.MirrorLaunchRequest;
import com.screenbridge.mirror.i18n.Messages;

import java.nio.file.Files;
import java.nio.file.Path;

public final class MirrorRequestFactory {
    private final Messages messages;

    public MirrorRequestFactory(Messages messages) {
        this.messages = messages;
    }

    public Path requireAdbPath(MirrorFormState formState) throws ValidationException {
        return requireExecutable(formState.adbPath(), "adb.exe");
    }

    public String requireWifiAddress(MirrorFormState formState) throws ValidationException {
        String address = normalizeOptional(formState.wifiAddress());
        if (address == null) {
            throw new ValidationException(
                    messages.get("error.connectFailed.title"),
                    messages.get("error.wifiAddressRequired", "192.168.1.10:5555"));
        }
        return address;
    }

    public MirrorLaunchRequest createLaunchRequest(MirrorFormState formState, String windowTitle)
            throws ValidationException {
        Path scrcpyPath = requireExecutable(formState.scrcpyPath(), "scrcpy.exe");
        Path adbPath = requireExecutable(formState.adbPath(), "adb.exe");

        DeviceInfo selectedDevice = formState.selectedDevice();
        if (selectedDevice == null) {
            throw new ValidationException(
                    messages.get("error.cannotStart.title"),
                    messages.get("error.noDeviceSelected.message"));
        }
        if (!selectedDevice.isReady()) {
            throw new ValidationException(
                    messages.get("error.cannotStart.title"),
                    messages.get("error.deviceNotReady.message"));
        }

        MirrorConfig config = new MirrorConfig(
                scrcpyPath,
                adbPath,
                selectedDevice.serial(),
                windowTitle,
                parsePositiveInteger(formState.maxSize(), "label.maxSize"),
                parsePositiveInteger(formState.maxFps(), "label.maxFps"),
                normalizeOptional(formState.videoBitRate()),
                formState.noAudio(),
                formState.turnScreenOff(),
                formState.stayAwake());

        return new MirrorLaunchRequest(config, adbPath);
    }

    private Path requireExecutable(String rawPath, String displayName) throws ValidationException {
        Path path = pathOrNull(rawPath);
        if (path == null || !Files.isRegularFile(path)) {
            throw new ValidationException(
                    messages.get("error.missingExecutable.title"),
                    messages.get("error.missingExecutable.message", displayName));
        }
        return path;
    }

    private Path pathOrNull(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return null;
        }
        return Path.of(rawPath.trim()).toAbsolutePath().normalize();
    }

    private Integer parsePositiveInteger(String rawValue, String fieldNameKey) throws ValidationException {
        String value = rawValue == null ? "" : rawValue.trim();
        if (value.isEmpty()) {
            return null;
        }

        String fieldName = messages.get(fieldNameKey);
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) {
                throw new ValidationException(
                        messages.get("error.invalidOptions.title"),
                        messages.get("validation.mustBePositive", fieldName));
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new ValidationException(
                    messages.get("error.invalidOptions.title"),
                    messages.get("validation.mustBeInteger", fieldName));
        }
    }

    private String normalizeOptional(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        return rawValue.trim();
    }
}
