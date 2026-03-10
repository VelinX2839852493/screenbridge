package com.screenbridge.mirror.settings;

import java.util.Optional;
import java.util.prefs.Preferences;

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
            // Ignore persistence failures and fall back to system defaults on next start.
        }
    }
}
