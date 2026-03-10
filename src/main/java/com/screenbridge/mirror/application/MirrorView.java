package com.screenbridge.mirror.application;

import com.screenbridge.mirror.domain.DeviceInfo;
import com.screenbridge.mirror.domain.MirrorFormState;

import java.util.List;

public interface MirrorView {
    MirrorFormState readFormState();

    void setScrcpyPath(String path);

    void setAdbPath(String path);

    void setDevices(List<DeviceInfo> devices);

    void setRefreshDevicesEnabled(boolean enabled);

    void setConnectWifiEnabled(boolean enabled);

    void setMirrorButtons(boolean startEnabled, boolean stopEnabled);

    void appendLog(String message);

    void showError(String title, String message);
}
