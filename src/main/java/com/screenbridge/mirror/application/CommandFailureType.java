package com.screenbridge.mirror.application;

/**
 * Identifies which adb operation failed so the UI can show a specific message.
 */
public enum CommandFailureType {
    LIST_DEVICES,
    CONNECT_WIRELESS,
    STOP_ADB_SERVER,
    PUSH_FILE
}
