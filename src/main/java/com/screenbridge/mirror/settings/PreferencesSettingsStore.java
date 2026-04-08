package com.screenbridge.mirror.settings;

import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * 基于 Java Preferences 的设置存储实现。
 */
public final class PreferencesSettingsStore implements SettingsStore {
    private final Preferences preferences;

    public PreferencesSettingsStore(Class<?> nodeClass) {
        this.preferences = Preferences.userNodeForPackage(nodeClass);
    }

    @Override
    public Optional<String> get(String key) {
        try {
            return Optional.ofNullable(preferences.get(key, null));
        } catch (SecurityException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, String value) {
        try {
            preferences.put(key, value);
        } catch (SecurityException ignored) {
            // 忽略持久化失败，下次启动时回退到系统默认设置。
        }
    }
}
