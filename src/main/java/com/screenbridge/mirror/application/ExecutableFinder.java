package com.screenbridge.mirror.application;

import java.nio.file.Path;
import java.util.Optional;

/**
 * 用于查找 scrcpy 和 adb 可执行文件的抽象接口。
 */
public interface ExecutableFinder {
    Optional<Path> findScrcpy();

    Optional<Path> findAdb(Path scrcpyPath);
}
