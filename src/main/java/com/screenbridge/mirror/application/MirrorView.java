package com.screenbridge.mirror.application;

import com.screenbridge.mirror.domain.DeviceInfo;
import com.screenbridge.mirror.domain.MirrorFormState;

import java.util.List;

/**
 * View contract used by the controller.
 */
public interface MirrorView {
    MirrorFormState readFormState();

    void setScrcpyPath(String path);

    void setAdbPath(String path);

    void setDevices(List<DeviceInfo> devices);

    void setRefreshDevicesEnabled(boolean enabled);

    void setConnectWifiEnabled(boolean enabled);

    void setMirrorButtons(boolean startEnabled, boolean stopEnabled);

    void setFileTransferEnabled(boolean enabled);

    void appendLog(String message);

    void showError(String title, String message);
}
