package com.screenbridge.mirror.application;

import java.nio.file.Path;
import java.util.Optional;

public interface ExecutableFinder {
    Optional<Path> findScrcpy();

    Optional<Path> findAdb(Path scrcpyPath);
}
