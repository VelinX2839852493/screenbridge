package com.screenbridge.mirror.infrastructure;

import com.screenbridge.mirror.application.ExecutableFinder;

import java.nio.file.Path;
import java.util.Optional;

public final class DefaultExecutableFinder implements ExecutableFinder {
    @Override
    public Optional<Path> findScrcpy() {
        return ExecutableLocator.findScrcpy();
    }

    @Override
    public Optional<Path> findAdb(Path scrcpyPath) {
        return ExecutableLocator.findAdb(scrcpyPath);
    }
}
