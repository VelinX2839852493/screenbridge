package com.screenbridge.mirror.application;

import java.io.IOException;

public final class CommandExecutionException extends IOException {
    private final CommandFailureType failureType;
    private final String commandOutput;

    public CommandExecutionException(CommandFailureType failureType, String commandOutput) {
        super(commandOutput);
        this.failureType = failureType;
        this.commandOutput = commandOutput == null ? "" : commandOutput;
    }

    public CommandFailureType failureType() {
        return failureType;
    }

    public String commandOutput() {
        return commandOutput;
    }
}
