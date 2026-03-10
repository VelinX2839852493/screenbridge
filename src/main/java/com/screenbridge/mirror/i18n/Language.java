package com.screenbridge.mirror.i18n;

import java.util.Locale;
import java.util.Optional;

public enum Language {
    ZH_CN("zh_CN", Locale.SIMPLIFIED_CHINESE, "中文"),
    EN("en", Locale.ENGLISH, "English");

    static final String SETTINGS_KEY = "ui.language";

    private final String preferenceValue;
    private final Locale locale;
    private final String displayName;

    Language(String preferenceValue, Locale locale, String displayName) {
        this.preferenceValue = preferenceValue;
        this.locale = locale;
        this.displayName = displayName;
    }

    public Locale locale() {
        return locale;
    }

    public String preferenceValue() {
        return preferenceValue;
    }

    public static Language resolve(String storedValue, Locale systemLocale) {
        return fromPreferenceValue(storedValue).orElse(fromLocale(systemLocale));
    }

    public static Language fromLocale(Locale locale) {
        if (locale != null && "zh".equalsIgnoreCase(locale.getLanguage())) {
            return ZH_CN;
        }
        return EN;
    }

    public static Optional<Language> fromPreferenceValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        for (Language language : values()) {
            if (language.preferenceValue.equalsIgnoreCase(value.trim())) {
                return Optional.of(language);
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return displayName;
    }
}
