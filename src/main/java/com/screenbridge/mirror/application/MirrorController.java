package com.screenbridge.mirror.application;

import com.screenbridge.mirror.domain.DeviceInfo;
import com.screenbridge.mirror.domain.MirrorFormState;
import com.screenbridge.mirror.domain.MirrorLaunchRequest;
import com.screenbridge.mirror.domain.MirrorProcessEvent;
import com.screenbridge.mirror.i18n.Language;
import com.screenbridge.mirror.i18n.LocaleManager;
import com.screenbridge.mirror.i18n.Messages;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Coordinates UI actions, request validation, background work, and process lifecycle.
 */
public final class MirrorController implements AutoCloseable {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final MirrorView view;
    private final MirrorService mirrorService;
    private final MirrorRequestFactory requestFactory;
    private final Messages messages;
    private final LocaleManager localeManager;
    private final ExecutorService backgroundExecutor;
    private final ExecutableFinder executableFinder;
    private final Map<Path, Boolean> adbRunningBeforeUse = new java.util.concurrent.ConcurrentHashMap<>();
    private final AtomicBoolean exitCleanupStarted = new AtomicBoolean();
    private final AtomicBoolean fileTransferInProgress = new AtomicBoolean();

    private volatile Process mirrorProcess;

    public MirrorController(
            MirrorView view,
            MirrorService mirrorService,
            MirrorRequestFactory requestFactory,
            Messages messages,
            LocaleManager localeManager,
            ExecutorService backgroundExecutor,
            ExecutableFinder executableFinder) {
        this.view = Objects.requireNonNull(view, "view");
        this.mirrorService = Objects.requireNonNull(mirrorService, "mirrorService");
        this.requestFactory = Objects.requireNonNull(requestFactory, "requestFactory");
        this.messages = Objects.requireNonNull(messages, "messages");
        this.localeManager = Objects.requireNonNull(localeManager, "localeManager");
        this.backgroundExecutor = Objects.requireNonNull(backgroundExecutor, "backgroundExecutor");
        this.executableFinder = Objects.requireNonNull(executableFinder, "executableFinder");
    }

    public void initialize() {
        autoFillExecutablePaths();
        if (!isBlank(view.readFormState().adbPath())) {
            onRefreshDevices();
        }
    }

    public void onLanguageSelected(Language language) {
        if (language != null) {
            localeManager.setLanguage(language);
        }
    }

    public void onRefreshDevices() {
        Path adbPath;
        try {
            adbPath = requestFactory.requireAdbPath(view.readFormState());
        } catch (ValidationException exception) {
            view.showError(exception.title(), exception.getMessage());
            return;
        }

        view.setRefreshDevicesEnabled(false);
        view.appendLog(messages.get("log.refreshDevices.start"));

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        rememberAdbState(adbPath);
                        return mirrorService.listDevices(adbPath);
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                }, backgroundExecutor)
                .whenComplete((devices, error) -> {
                    view.setRefreshDevicesEnabled(true);
                    if (error != null) {
                        String localizedMessage = localizeError(rootCause(error));
                        view.appendLog(messages.get("log.refreshDevices.failure", localizedMessage));
                        view.showError(messages.get("error.refreshFailed.title"), localizedMessage);
                        return;
                    }

                    view.setDevices(devices);
                    view.appendLog(devices.isEmpty()
                            ? messages.get("log.devices.none")
                            : messages.get("log.devices.count", devices.size()));
                });
    }

    public void onConnectWifi() {
        MirrorFormState formState = view.readFormState();

        Path adbPath;
        String address;
        try {
            adbPath = requestFactory.requireAdbPath(formState);
            address = requestFactory.requireWifiAddress(formState);
        } catch (ValidationException exception) {
            view.showError(exception.title(), exception.getMessage());
            return;
        }

        view.setConnectWifiEnabled(false);
        view.appendLog(messages.get("log.connectWifi.start", address));

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        rememberAdbState(adbPath);
                        return mirrorService.connectWireless(adbPath, address);
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                }, backgroundExecutor)
                .whenComplete((output, error) -> {
                    view.setConnectWifiEnabled(true);
                    if (error != null) {
                        String localizedMessage = localizeError(rootCause(error));
                        view.appendLog(messages.get("log.connectWifi.failure", localizedMessage));
                        view.showError(messages.get("error.wifiConnectFailed.title"), localizedMessage);
                        return;
                    }

                    view.appendLog(output);
                    onRefreshDevices();
                });
    }

    public void onStartMirror() {
        Process process = mirrorProcess;
        if (process != null && process.isAlive()) {
            view.showError(messages.get("error.alreadyRunning.title"), messages.get("error.alreadyRunning.message"));
            return;
        }

        MirrorFormState formState = view.readFormState();
        MirrorLaunchRequest launchRequest;
        try {
            launchRequest = requestFactory.createLaunchRequest(
                    formState,
                    buildWindowTitle(formState.selectedDevice()));
        } catch (ValidationException exception) {
            view.showError(exception.title(), exception.getMessage());
            return;
        }

        rememberAdbState(launchRequest.adbPath());
        logResolvedWindowFit(launchRequest, formState);

        try {
            mirrorProcess = mirrorService.startMirroring(launchRequest.config(), this::handleProcessEvent);
            view.appendLog(messages.get("log.scrcpy.started"));
            view.setMirrorButtons(false, true);
            watchMirrorExit(mirrorProcess);
        } catch (IOException exception) {
            String message = rootMessage(exception);
            view.appendLog(messages.get("log.scrcpy.startFailed", message));
            view.showError(messages.get("error.startFailed.title"), message);
        }
    }

    public void onStopMirror() {
        stopMirrorProcess(true);
    }

    public void onPushFiles(List<Path> files) {
        List<Path> transferableFiles = normalizeTransferFiles(files);
        if (transferableFiles.isEmpty()) {
            view.showError(
                    messages.get("error.fileTransferFailed.title"),
                    messages.get("error.fileTransferNoFiles.message"));
            return;
        }
        if (!fileTransferInProgress.compareAndSet(false, true)) {
            view.showError(
                    messages.get("error.fileTransferInProgress.title"),
                    messages.get("error.fileTransferInProgress.message"));
            return;
        }

        MirrorFormState formState = view.readFormState();
        Path adbPath;
        DeviceInfo device;
        String pushTarget;
        try {
            adbPath = requestFactory.requireAdbPath(formState);
            device = requestFactory.requireReadyDevice(formState);
            pushTarget = requestFactory.resolvePushTarget(formState);
        } catch (ValidationException exception) {
            fileTransferInProgress.set(false);
            view.showError(exception.title(), exception.getMessage());
            return;
        }

        rememberAdbState(adbPath);
        view.setFileTransferEnabled(false);
        view.appendLog(messages.get("log.fileTransfer.start", transferableFiles.size(), pushTarget));

        CompletableFuture
                .runAsync(() -> pushFiles(adbPath, device.serial(), pushTarget, transferableFiles), backgroundExecutor)
                .whenComplete((unused, error) -> {
                    fileTransferInProgress.set(false);
                    view.setFileTransferEnabled(true);
                    if (error != null) {
                        String localizedMessage = localizeError(rootCause(error));
                        view.appendLog(messages.get("log.fileTransfer.failure", localizedMessage));
                        view.showError(messages.get("error.fileTransferFailed.title"), localizedMessage);
                        return;
                    }

                    view.appendLog(messages.get("log.fileTransfer.success", transferableFiles.size(), pushTarget));
                });
    }

    public void onExit() {
        if (!exitCleanupStarted.compareAndSet(false, true)) {
            return;
        }

        stopMirrorProcess(false);
        stopOwnedAdbServers();
        backgroundExecutor.shutdownNow();
    }

    @Override
    public void close() {
        onExit();
    }

    private void autoFillExecutablePaths() {
        executableFinder.findScrcpy().ifPresent(path -> view.setScrcpyPath(path.toString()));

        MirrorFormState formState = view.readFormState();
        Path scrcpyPath = pathOrNull(formState.scrcpyPath());
        executableFinder.findAdb(scrcpyPath).ifPresent(path -> view.setAdbPath(path.toString()));

        MirrorFormState updatedState = view.readFormState();
        if (isBlank(updatedState.scrcpyPath())) {
            view.appendLog(messages.get("log.scrcpy.notFound"));
        }
        if (isBlank(updatedState.adbPath())) {
            view.appendLog(messages.get("log.adb.notFound"));
        }
    }

    private void watchMirrorExit(Process process) {
        backgroundExecutor.execute(() -> {
            try {
                int exitCode = process.waitFor();
                view.appendLog(messages.get("log.scrcpy.exited", exitCode));
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } finally {
                if (mirrorProcess == process) {
                    mirrorProcess = null;
                }
                view.setMirrorButtons(true, false);
            }
        });
    }

    private void stopMirrorProcess(boolean logAction) {
        Process process = mirrorProcess;
        if (process == null) {
            return;
        }

        if (process.isAlive()) {
            mirrorService.stopProcessTree(process, this::handleProcessEvent);
        }

        if (mirrorProcess == process) {
            mirrorProcess = null;
        }
        if (logAction) {
            view.appendLog(messages.get("log.scrcpy.stopRequested"));
        }
        view.setMirrorButtons(true, false);
    }

    private void stopOwnedAdbServers() {
        for (Map.Entry<Path, Boolean> entry : adbRunningBeforeUse.entrySet()) {
            Path adbPath = entry.getKey();
            boolean wasRunningBeforeUse = Boolean.TRUE.equals(entry.getValue());
            if (wasRunningBeforeUse || !mirrorService.isProcessRunning(adbPath)) {
                continue;
            }

            try {
                mirrorService.stopAdbServer(adbPath);
            } catch (IOException exception) {
                // Ignore cleanup failures during exit.
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void rememberAdbState(Path adbPath) {
        if (adbPath == null) {
            return;
        }

        Path normalizedPath = adbPath.toAbsolutePath().normalize();
        adbRunningBeforeUse.computeIfAbsent(normalizedPath, mirrorService::isProcessRunning);
    }

    private void handleProcessEvent(MirrorProcessEvent event) {
        switch (event.type()) {
            case OUTPUT -> view.appendLog(stamp(event.detail()));
            case FORCE_SHUTDOWN -> view.appendLog(stamp(messages.get("log.scrcpy.forceShutdown")));
            case FORCE_SHUTDOWN_FAILED -> view.appendLog(stamp(messages.get("log.scrcpy.forceShutdownFailed")));
            case OUTPUT_READ_FAILED ->
                    view.appendLog(stamp(messages.get("error.scrcpy.readOutputFailed", event.detail())));
        }
    }

    private String buildWindowTitle(DeviceInfo device) {
        if (device != null && device.serial() != null && !device.serial().isBlank()) {
            return messages.get("scrcpy.windowTitle.withDevice", device.serial());
        }
        return messages.get("scrcpy.windowTitle");
    }

    private String localizeError(Throwable throwable) {
        if (throwable instanceof CommandExecutionException exception) {
            return switch (exception.failureType()) {
                case LIST_DEVICES -> messages.get("error.adb.devicesFailed", exception.commandOutput());
                case CONNECT_WIRELESS -> messages.get("error.adb.connectFailed", exception.commandOutput());
                case STOP_ADB_SERVER -> messages.get("error.adb.killServerFailed", exception.commandOutput());
                case PUSH_FILE -> messages.get("error.adb.pushFailed", exception.commandOutput());
            };
        }
        return rootMessage(throwable);
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private String rootMessage(Throwable throwable) {
        return throwable.getMessage() == null ? throwable.toString() : throwable.getMessage();
    }

    private String stamp(String message) {
        return "[" + LocalTime.now().format(TIME_FORMAT) + "] " + message;
    }

    private Path pathOrNull(String rawPath) {
        if (isBlank(rawPath)) {
            return null;
        }
        return Path.of(rawPath.trim()).toAbsolutePath().normalize();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void pushFiles(Path adbPath, String deviceSerial, String pushTarget, List<Path> files) {
        for (Path file : files) {
            try {
                view.appendLog(messages.get("log.fileTransfer.itemStart", file.getFileName()));
                mirrorService.pushFile(adbPath, deviceSerial, file, pushTarget);
                view.appendLog(messages.get("log.fileTransfer.itemSuccess", file.getFileName()));
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(exception);
            }
        }
    }

    private List<Path> normalizeTransferFiles(List<Path> files) {
        List<Path> normalized = new ArrayList<>();
        if (files == null) {
            return normalized;
        }

        for (Path file : files) {
            if (file == null) {
                continue;
            }

            Path absoluteFile = file.toAbsolutePath().normalize();
            if (Files.isRegularFile(absoluteFile)) {
                normalized.add(absoluteFile);
            }
        }
        return normalized;
    }

    private void logResolvedWindowFit(MirrorLaunchRequest launchRequest, MirrorFormState formState) {
        if (!formState.fitWindowToScreen()
                || launchRequest.config().windowWidth() == null
                || launchRequest.config().windowHeight() == null) {
            return;
        }

        view.appendLog(messages.get(
                "log.windowFit.applied",
                launchRequest.config().windowWidth(),
                launchRequest.config().windowHeight()));
    }
}
