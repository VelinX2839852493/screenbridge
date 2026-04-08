package com.screenbridge.mirror.application;

import com.screenbridge.mirror.domain.DeviceInfo;
import com.screenbridge.mirror.domain.MirrorConfig;
import com.screenbridge.mirror.domain.MirrorProcessEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * Defines the service operations that interact with adb and scrcpy.
 */
public interface MirrorService {
    List<DeviceInfo> listDevices(Path adbPath) throws IOException, InterruptedException;

    String connectWireless(Path adbPath, String address) throws IOException, InterruptedException;

    void stopAdbServer(Path adbPath) throws IOException, InterruptedException;

    boolean isProcessRunning(Path executablePath);

    void pushFile(Path adbPath, String deviceSerial, Path localFile, String remoteTarget)
            throws IOException, InterruptedException;

    void stopProcessTree(Process process, Consumer<MirrorProcessEvent> eventConsumer);

    Process startMirroring(MirrorConfig config, Consumer<MirrorProcessEvent> eventConsumer) throws IOException;
}
