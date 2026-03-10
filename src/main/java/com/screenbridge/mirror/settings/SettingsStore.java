package com.screenbridge.mirror.settings;

import java.util.Optional;

public interface SettingsStore {
    Optional<String> get(String key);

    void put(String key, String value);
}
