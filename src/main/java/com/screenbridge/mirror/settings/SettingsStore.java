package com.screenbridge.mirror.settings;

import java.util.Optional;

/**
 * 定义用户设置的读取和保存接口。
 */
public interface SettingsStore {
    Optional<String> get(String key);

    void put(String key, String value);
}
