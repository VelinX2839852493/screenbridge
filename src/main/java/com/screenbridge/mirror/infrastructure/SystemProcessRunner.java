package com.screenbridge.mirror.infrastructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public final class SystemProcessRunner implements ProcessRunner {
    @Override
    public CommandResult run(List<String> command, Path workingDirectory) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        if (workingDirectory != null) {
            processBuilder.directory(workingDirectory.toFile());
        }
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        int exitCode = process.waitFor();
        return new CommandResult(exitCode, output.toString().trim());
    }

    @Override
    public Process start(List<String> command, Path workingDirectory, Path pathPrefix) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        if (workingDirectory != null) {
            processBuilder.directory(workingDirectory.toFile());
        }
        processBuilder.redirectErrorStream(true);

        if (pathPrefix != null) {
            String currentPath = processBuilder.environment().getOrDefault("Path", "");
            processBuilder.environment().put(
                    "Path",
                    pathPrefix + System.getProperty("path.separator") + currentPath);
        }

        return processBuilder.start();
    }
}
