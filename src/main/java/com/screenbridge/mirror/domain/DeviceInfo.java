package com.screenbridge.mirror.domain;

public record DeviceInfo(String serial, String state, String displayName) {
    public boolean isReady() {
        return "device".equalsIgnoreCase(state);
    }

    @Override
    public String toString() {
        if (isReady()) {
            return displayName + " (" + serial + ")";
        }
        return displayName + " (" + serial + ", state: " + state + ")";
    }
}
