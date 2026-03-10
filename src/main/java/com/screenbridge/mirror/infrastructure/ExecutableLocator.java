package com.screenbridge.mirror.infrastructure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class ExecutableLocator {
    private ExecutableLocator() {
    }

    public static Optional<Path> findScrcpy() {
        return findBundledExecutable("scrcpy.exe");
    }

    public static Optional<Path> findAdb(Path scrcpyPath) {
        if (scrcpyPath != null) {
            Optional<Path> sibling = existing(scrcpyPath.getParent().resolve("adb.exe"));
            if (sibling.isPresent()) {
                return sibling;
            }
        }

        return findBundledExecutable("adb.exe");
    }

    private static Optional<Path> findBundledExecutable(String executableName) {
        for (Path searchRoot : bundledSearchRoots()) {
            Optional<Path> existing = existing(searchRoot.resolve(executableName));
            if (existing.isPresent()) {
                return existing;
            }
            Optional<Path> nested = findInBundledDirectory(searchRoot, executableName);
            if (nested.isPresent()) {
                return nested;
            }
        }
        return Optional.empty();
    }

    private static Optional<Path> findInBundledDirectory(Path searchRoot, String executableName) {
        if (searchRoot == null || !Files.isDirectory(searchRoot)) {
            return Optional.empty();
        }
        try (Stream<Path> children = Files.list(searchRoot)) {
            return children
                    .filter(Files::isDirectory)
                    .filter(path -> {
                        Path fileName = path.getFileName();
                        return fileName != null && fileName.toString().toLowerCase().startsWith("scrcpy");
                    })
                    .map(path -> path.resolve(executableName))
                    .filter(Files::isRegularFile)
                    .map(path -> path.toAbsolutePath().normalize())
                    .findFirst();
        } catch (IOException exception) {
            return Optional.empty();
        }
    }

    private static List<Path> bundledSearchRoots() {
        Path workingDirectory = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        return bundledSearchRoots(workingDirectory, findCodeSourceDirectory());
    }

    static List<Path> bundledSearchRoots(Path workingDirectory, Optional<Path> codeSourceDirectory) {
        List<Path> candidates = new ArrayList<>();

        addCandidate(candidates, workingDirectory.resolve("src").resolve("main").resolve("resources").resolve("packaging"));
        codeSourceDirectory.ifPresent(directory -> addCandidate(candidates, runtimeSearchRoot(directory)));

        return candidates;
    }

    private static Optional<Path> findCodeSourceDirectory() {
        try {
            if (ExecutableLocator.class.getProtectionDomain() == null
                    || ExecutableLocator.class.getProtectionDomain().getCodeSource() == null
                    || ExecutableLocator.class.getProtectionDomain().getCodeSource().getLocation() == null) {
                return Optional.empty();
            }
            Path location = Path.of(ExecutableLocator.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return Optional.of(Files.isDirectory(location) ? location : location.getParent());
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private static Optional<Path> existing(Path candidate) {
        if (candidate != null && Files.isRegularFile(candidate)) {
            return Optional.of(candidate.toAbsolutePath().normalize());
        }
        return Optional.empty();
    }

    private static void addCandidate(List<Path> candidates, Path candidate) {
        if (candidate == null) {
            return;
        }

        Path normalized = candidate.toAbsolutePath().normalize();
        if (!candidates.contains(normalized)) {
            candidates.add(normalized);
        }
    }

    private static Path runtimeSearchRoot(Path codeSourceDirectory) {
        Path packagingDirectory = codeSourceDirectory.resolve("packaging");
        if (Files.isDirectory(packagingDirectory)) {
            return packagingDirectory;
        }
        return codeSourceDirectory;
    }
}
