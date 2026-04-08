package com.screenbridge.mirror.domain;

/**
 * Represents the scrcpy input injection mode for keyboard or mouse control.
 */
public enum InputMode {
    DEFAULT(null, "inputMode.default"),
    SDK("sdk", "inputMode.sdk"),
    UHID("uhid", "inputMode.uhid"),
    AOA("aoa", "inputMode.aoa"),
    DISABLED("disabled", "inputMode.disabled");

    private final String cliValue;
    private final String messageKey;

    InputMode(String cliValue, String messageKey) {
        this.cliValue = cliValue;
        this.messageKey = messageKey;
    }

    public String cliValue() {
        return cliValue;
    }

    public String messageKey() {
        return messageKey;
    }

    public boolean usesCliValue() {
        return cliValue != null;
    }
}
