package com.screenbridge.mirror.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * 负责加载多语言资源，并按当前语言格式化文本。
 */
public final class Messages {
    private static final String BUNDLE_BASE_NAME = "i18n.messages";

    private final LocaleManager localeManager;

    private volatile Language currentLanguage;
    private volatile ResourceBundle bundle;

    public Messages(LocaleManager localeManager) {
        this.localeManager = Objects.requireNonNull(localeManager, "localeManager");
        this.currentLanguage = localeManager.currentLanguage();
        this.bundle = loadBundle(currentLanguage);
        this.localeManager.addListener(this::onLanguageChanged);
    }

    public Language currentLanguage() {
        return currentLanguage;
    }

    public String get(String key, Object... args) {
        String pattern = bundle.getString(key);
        if (args == null || args.length == 0) {
            return pattern;
        }
        MessageFormat formatter = new MessageFormat(pattern, currentLanguage.locale());
        return formatter.format(args);
    }

    static ResourceBundle bundleFor(Language language) {
        return loadBundle(language);
    }

    private void onLanguageChanged(Language language) {
        currentLanguage = language;
        bundle = loadBundle(language);
    }

    private static ResourceBundle loadBundle(Language language) {
        Locale locale = language == null ? Locale.getDefault() : language.locale();
        return ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale);
    }
}
