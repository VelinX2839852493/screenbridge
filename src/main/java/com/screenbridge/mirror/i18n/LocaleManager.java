package com.screenbridge.mirror.i18n;

import com.screenbridge.mirror.settings.SettingsStore;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 管理当前界面语言、持久化保存，并在语言切换时通知监听者。
 */
public final class LocaleManager {
    private final SettingsStore settingsStore;
    private final Locale systemLocale;
    private final List<Consumer<Language>> listeners = new CopyOnWriteArrayList<>();

    private volatile Language currentLanguage;

    public LocaleManager(SettingsStore settingsStore, Locale systemLocale) {
        this.settingsStore = Objects.requireNonNull(settingsStore, "settingsStore");
        this.systemLocale = Objects.requireNonNull(systemLocale, "systemLocale");
        this.currentLanguage = Language.resolve(
                settingsStore.get(Language.SETTINGS_KEY).orElse(null),
                systemLocale);
    }

    public Language currentLanguage() {
        return currentLanguage;
    }

    public void setLanguage(Language language) {
        Language nextLanguage = Objects.requireNonNull(language, "language");
        if (nextLanguage == currentLanguage) {
            return;
        }

        currentLanguage = nextLanguage;
        settingsStore.put(Language.SETTINGS_KEY, nextLanguage.preferenceValue());
        for (Consumer<Language> listener : listeners) {
            listener.accept(nextLanguage);
        }
    }

    public void addListener(Consumer<Language> listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void removeListener(Consumer<Language> listener) {
        listeners.remove(listener);
    }

    Locale systemLocale() {
        return systemLocale;
    }
}
