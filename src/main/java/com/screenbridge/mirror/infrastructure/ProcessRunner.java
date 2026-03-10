package com.screenbridge.mirror.infrastructure;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface ProcessRunner {
    CommandResult run(List<String> command, Path workingDirectory) throws IOException, InterruptedException;

    Process start(List<String> command, Path workingDirectory, Path pathPrefix) throws IOException;
}
