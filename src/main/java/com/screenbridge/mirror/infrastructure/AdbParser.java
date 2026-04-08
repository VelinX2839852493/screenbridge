package com.screenbridge.mirror.infrastructure;

import com.screenbridge.mirror.domain.DeviceInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 将 adb devices 输出解析成界面可用的设备对象。
 */
public final class AdbParser {
    private AdbParser() {
    }

    public static List<DeviceInfo> parseDevices(String output) {
        List<DeviceInfo> devices = new ArrayList<>();
        for (String rawLine : output.split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty()
                    || line.startsWith("List of devices attached")
                    || line.startsWith("* daemon")) {
                continue;
            }

            String[] tokens = line.split("\\s+");
            if (tokens.length < 2) {
                continue;
            }

            String serial = tokens[0];
            String state = tokens[1];
            String model = serial;
            for (String token : tokens) {
                if (token.startsWith("model:")) {
                    model = token.substring("model:".length()).replace('_', ' ');
                    break;
                }
            }
            devices.add(new DeviceInfo(serial, state, model));
        }
        return devices;
    }
}
