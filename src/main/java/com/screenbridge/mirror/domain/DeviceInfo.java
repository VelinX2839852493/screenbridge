package com.screenbridge.mirror.domain;

/**
 * 表示一个通过 adb 查询到的设备信息。
 */
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
