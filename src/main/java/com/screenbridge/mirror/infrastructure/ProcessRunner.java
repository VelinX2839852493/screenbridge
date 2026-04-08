package com.screenbridge.mirror.infrastructure;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * 抽象外部进程的同步执行和异步启动能力。
 */
public interface ProcessRunner {
    CommandResult run(List<String> command, Path workingDirectory) throws IOException, InterruptedException;

    Process start(List<String> command, Path workingDirectory, Path pathPrefix) throws IOException;
}
