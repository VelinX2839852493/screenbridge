package com.screenbridge.mirror.infrastructure;

import com.screenbridge.mirror.application.CommandExecutionException;
import com.screenbridge.mirror.application.CommandFailureType;
import com.screenbridge.mirror.application.MirrorService;
import com.screenbridge.mirror.domain.DeviceInfo;
import com.screenbridge.mirror.domain.MirrorConfig;
import com.screenbridge.mirror.domain.MirrorProcessEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class AndroidMirrorService implements MirrorService {
    private final ProcessRunner processRunner;

    public AndroidMirrorService() {
        this(new SystemProcessRunner());
    }

    AndroidMirrorService(ProcessRunner processRunner) {
        this.processRunner = processRunner;
    }

    @Override
    public List<DeviceInfo> listDevices(Path adbPath) throws IOException, InterruptedException {
        CommandResult result = processRunner.run(List.of(adbPath.toString(), "devices", "-l"), adbPath.getParent());
        if (result.exitCode() != 0) {
            throw new CommandExecutionException(CommandFailureType.LIST_DEVICES, result.output());
        }
        return AdbParser.parseDevices(result.output());
    }

    @Override
    public String connectWireless(Path adbPath, String address) throws IOException, InterruptedException {
        CommandResult result = processRunner.run(List.of(adbPath.toString(), "connect", address), adbPath.getParent());
        if (result.exitCode() != 0) {
            throw new CommandExecutionException(CommandFailureType.CONNECT_WIRELESS, result.output());
        }
        return result.output();
    }

    @Override
    public void stopAdbServer(Path adbPath) throws IOException, InterruptedException {
        CommandResult result = processRunner.run(List.of(adbPath.toString(), "kill-server"), adbPath.getParent());
        if (result.exitCode() != 0) {
            throw new CommandExecutionException(CommandFailureType.STOP_ADB_SERVER, result.output());
        }
    }

    @Override
    public boolean isProcessRunning(Path executablePath) {
        Path normalizedPath = normalizePath(executablePath);
        if (normalizedPath == null) {
            return false;
        }

        return ProcessHandle.allProcesses()
                .filter(ProcessHandle::isAlive)
                .map(this::commandPath)
                .flatMap(Optional::stream)
                .anyMatch(commandPath -> sameExecutable(commandPath, normalizedPath));
    }

    @Override
    public void stopProcessTree(Process process, Consumer<MirrorProcessEvent> eventConsumer) {
        if (process == null) {
            return;
        }

        ProcessHandle handle = process.toHandle();
        destroyDescendants(handle, false);
        process.destroy();
        if (waitForExit(process, 3)) {
            return;
        }

        if (eventConsumer != null) {
            eventConsumer.accept(MirrorProcessEvent.forceShutdown());
        }

        destroyDescendants(handle, true);
        process.destroyForcibly();
        if (!waitForExit(process, 2) && eventConsumer != null) {
            eventConsumer.accept(MirrorProcessEvent.forceShutdownFailed());
        }
    }

    @Override
    public Process startMirroring(MirrorConfig config, Consumer<MirrorProcessEvent> eventConsumer) throws IOException {
        Process process = processRunner.start(
                ScrcpyCommandBuilder.build(config),
                config.scrcpyPath().getParent(),
                config.adbPath() == null ? null : config.adbPath().getParent());
        streamOutput(process, eventConsumer);
        return process;
    }

    private void streamOutput(Process process, Consumer<MirrorProcessEvent> eventConsumer) {
        if (eventConsumer == null) {
            return;
        }

        Thread readerThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    eventConsumer.accept(MirrorProcessEvent.output(line));
                }
            } catch (IOException exception) {
                eventConsumer.accept(MirrorProcessEvent.outputReadFailed(exception.getMessage()));
            }
        }, "scrcpy-output");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private Optional<Path> commandPath(ProcessHandle handle) {
        return handle.info()
                .command()
                .flatMap(command -> {
                    try {
                        return Optional.of(Path.of(command).toAbsolutePath().normalize());
                    } catch (InvalidPathException exception) {
                        return Optional.empty();
                    }
                });
    }

    private void destroyDescendants(ProcessHandle handle, boolean forcibly) {
        handle.descendants()
                .sorted(Comparator.comparingLong(ProcessHandle::pid).reversed())
                .forEach(descendant -> {
                    if (!descendant.isAlive()) {
                        return;
                    }
                    if (forcibly) {
                        descendant.destroyForcibly();
                        return;
                    }
                    descendant.destroy();
                });
    }

    private boolean waitForExit(Process process, long timeoutSeconds) {
        try {
            return process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return !process.isAlive();
        }
    }

    private Path normalizePath(Path path) {
        if (path == null) {
            return null;
        }
        return path.toAbsolutePath().normalize();
    }

    private boolean sameExecutable(Path left, Path right) {
        try {
            return java.nio.file.Files.isSameFile(left, right);
        } catch (IOException exception) {
            return left.toString().equalsIgnoreCase(right.toString());
        }
    }
}
