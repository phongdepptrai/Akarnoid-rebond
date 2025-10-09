package com.arcade.arkanoid.localization;

import com.arcade.arkanoid.engine.settings.SettingsManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public class LocalizationService {
    private static final String DEFAULT_BUNDLE = "i18n/messages_en.json";
    private final ObjectMapper mapper = new ObjectMapper();
    private final SettingsManager settingsManager;

    private Locale activeLocale;
    private Map<String, String> messages = Collections.emptyMap();

    public LocalizationService(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        setLocale(settingsManager.resolveLocale());
    }

    public void setLocale(Locale locale) {
        if (locale == null) {
            locale = Locale.ENGLISH;
        }
        this.activeLocale = locale;
        this.messages = loadBundleForLocale(locale);
        settingsManager.setLocale(locale);
    }

    public Locale getActiveLocale() {
        return activeLocale;
    }

    public String translate(String key, Object... args) {
        String pattern = messages.getOrDefault(key, key);
        if (args == null || args.length == 0) {
            return pattern;
        }
        return MessageFormat.format(pattern, args);
    }

    private Map<String, String> loadBundleForLocale(Locale locale) {
        String resourcePath = "i18n/messages_" + locale.getLanguage() + ".json";
        Map<String, String> bundle = readBundle(resourcePath);
        if (bundle.isEmpty() && locale.getCountry() != null && !locale.getCountry().isEmpty()) {
            resourcePath = "i18n/messages_" + locale.getLanguage() + "_" + locale.getCountry() + ".json";
            bundle = readBundle(resourcePath);
        }
        if (bundle.isEmpty()) {
            bundle = readBundle(DEFAULT_BUNDLE);
        }
        return bundle;
    }

    private Map<String, String> readBundle(String resourcePath) {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return Collections.emptyMap();
            }
            return mapper.readValue(stream, new TypeReference<Map<String, String>>() {
            });
        } catch (IOException e) {
            System.err.println("Failed to load localization bundle " + resourcePath + ": " + e.getMessage());
            return Collections.emptyMap();
        }
    }
}
